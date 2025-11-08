package com.hackathon.tomolow.domain.ticker.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.tomolow.domain.market.entity.ExchangeType;
import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.market.repository.MarketRepository;
import com.hackathon.tomolow.domain.market.service.RankingService;
import com.hackathon.tomolow.domain.ticker.dto.TickerMessage;
import com.hackathon.tomolow.global.redis.RedisUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpbitTickerService {

  private final ObjectMapper om = new ObjectMapper();
  private final SimpMessagingTemplate messagingTemplate;
  private final RedisUtil redisUtil;
  private final MarketRepository marketRepository;

  private final PriceTickDispatcher tickDispatcher; // 추가: 틱을 매칭기로 넘겨줄 컴포넌트
  // private final PortfolioIncrementService portfolioIncrementService; // 추가: 홈화면 포트폴리오 증분 누적

  // 심볼→이름 캐시
  private final Map<String, String> nameCache = new ConcurrentHashMap<>();

  // 현재 구독 중인 코드 집합(변경 감지용)
  private volatile Set<String> subscribedCodes = ConcurrentHashMap.newKeySet();

  private OkHttpClient client;
  private WebSocket webSocket;

  private static final String UPBIT_WS_URL = "wss://api.upbit.com/websocket/v1";
  private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

  // 업비트에 한 번에 너무 많은 코드를 보내지 않도록 배치로 전송 (안전하게 80개 단위)
  private static final int SUBSCRIBE_BATCH_SIZE = 80;

  private final RankingService rankingService;

  @PostConstruct
  public void connect() {
    client =
        new OkHttpClient.Builder()
            .pingInterval(15, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build();

    Request request = new Request.Builder().url(UPBIT_WS_URL).build();
    webSocket =
        client.newWebSocket(
            request,
            new WebSocketListener() {
              @Override
              public void onOpen(WebSocket webSocket, Response response) {
                log.info("Connected to Upbit WS: {}", response);
                // DB에서 코드 읽어와 구독
                List<String> codes = loadUpbitCodesFromDB();
                subscribeCodes(webSocket, codes);
                subscribedCodes = new HashSet<>(codes);
              }

              @Override
              public void onMessage(WebSocket ws, String text) {
                handleMessage(text.getBytes());
              }

              @Override
              public void onMessage(WebSocket ws, okio.ByteString bytes) {
                handleMessage(bytes.toByteArray());
              }

              @Override
              public void onFailure(WebSocket ws, Throwable t, Response resp) {
                log.error("Upbit WS failure", t);
                reconnect();
              }

              @Override
              public void onClosed(WebSocket ws, int code, String reason) {
                log.warn("Upbit WS closed: {} {}", code, reason);
                reconnect();
              }
            });
  }

  @PreDestroy
  public void shutdown() {
    try {
      if (webSocket != null) {
        webSocket.close(1000, "shutdown");
      }
      if (client != null) {
        client.dispatcher().executorService().shutdown();
      }
    } catch (Exception ignored) {
    }
  }

  private void reconnect() {
    try {
      Thread.sleep(2000L);
    } catch (InterruptedException ignored) {
    }
    connect();
  }

  /** DB에서 업비트 심볼 목록 로드 */
  private List<String> loadUpbitCodesFromDB() {
    List<Market> markets = marketRepository.findAllByExchangeType(ExchangeType.UPBIT);
    List<String> codes =
        markets.stream()
            .map(Market::getSymbol) // 예: "KRW-BTC"
            .filter(s -> s != null && !s.isBlank())
            .distinct()
            .sorted()
            .toList();
    log.info("Loaded {} Upbit markets from DB", codes.size());
    return codes;
  }

  /** 코드 목록을 배치로 구독 전송 */
  private void subscribeCodes(WebSocket ws, List<String> codes) {
    if (codes.isEmpty()) {
      return;
    }

    for (int i = 0; i < codes.size(); i += SUBSCRIBE_BATCH_SIZE) {
      List<String> batch = codes.subList(i, Math.min(i + SUBSCRIBE_BATCH_SIZE, codes.size()));
      try {
        var ticket = Map.of("ticket", "tomolow-" + System.currentTimeMillis());
        var tickerReq = Map.of("type", "ticker", "codes", batch);
        String payload = om.writeValueAsString(List.of(ticket, tickerReq));
        ws.send(payload);
        log.info("Subscribed batch ({} codes): {}", batch.size(), batch);
      } catch (Exception e) {
        log.error("Subscribe payload error", e);
      }
    }
  }

  private void handleMessage(byte[] raw) {
    try {
      Map<String, Object> m = om.readValue(raw, new TypeReference<>() {});
      String symbol = (String) m.get("code"); // ex) KRW-BTC
      BigDecimal tradePrice = toBig(m.get("trade_price"));
      BigDecimal signedChangeRate = toBig(m.get("signed_change_rate"));
      BigDecimal changePrice = toBig(m.get("change_price")); // 전일대비 원
      BigDecimal prevClose = toBig(m.get("prev_closing_price")); // 전일 종가
      BigDecimal accVol24h = toBig(m.get("acc_trade_volume_24h"));
      BigDecimal accAmt24h = toBig(m.get("acc_trade_price_24h")); // ✅ 추가
      long ts = ((Number) m.get("timestamp")).longValue();

      String marketName =
          nameCache.computeIfAbsent(
              symbol, s -> marketRepository.findBySymbol(s).map(Market::getName).orElse(s));

      TickerMessage dto =
          TickerMessage.builder()
              .market(symbol)
              .marketName(marketName)
              .tradePrice(tradePrice)
              .changeRate(signedChangeRate)
              .changePrice(changePrice)
              .prevClose(prevClose)
              .accVolume(accVol24h)
              .accTradePrice24h(accAmt24h) // ✅
              .tradeTimestamp(ts)
              .build();

      redisUtil.setData("last_price:" + symbol, tradePrice.toPlainString());
      redisUtil.setData("ticker:" + symbol, om.writeValueAsString(dto));

      // ✅ 랭킹 업데이트 트리거
      rankingService.onTick(dto);

      messagingTemplate.convertAndSend("/topic/ticker/" + symbol, dto);

      // ✅ 틱이 온 심볼만 매칭 트리거(논블로킹)
      tickDispatcher.onTick(symbol, tradePrice);

      // ✅ (추가) 포트폴리오 증분 누적 -> 스케줄러 삭제하면서, 증분로직 삭제, 증분 메서드 삭제.
      // portfolioIncrementService.onTick(symbol, tradePrice);

    } catch (Exception e) {
      log.warn("Ticker parse/broadcast error: {}", e.getMessage());
    }
  }

  private BigDecimal toBig(Object v) {
    if (v == null) {
      return BigDecimal.ZERO;
    }
    if (v instanceof Number n) {
      return BigDecimal.valueOf(n.doubleValue());
    }
    return new BigDecimal(String.valueOf(v));
  }

  /** 💡 마켓 테이블이 변경되었는지 5분마다 검사 → 목록이 달라지면 재구독 (필요 시 주기/조건은 자유롭게 조절) */
  @Scheduled(fixedDelay = 5 * 60 * 1000L)
  public void refreshSubscriptionIfNeeded() {
    try {
      List<String> current = loadUpbitCodesFromDB();
      Set<String> now = new HashSet<>(current);
      if (!now.equals(subscribedCodes)) {
        log.info(
            "Market set changed. Re-subscribing. old={}, new={}",
            subscribedCodes.size(),
            now.size());
        // 가장 간단/안전하게 전체 재연결
        reconnect();
      }
    } catch (Exception e) {
      log.warn("refreshSubscriptionIfNeeded error: {}", e.getMessage());
    }
  }
}
