package com.hackathon.tomolow.domain.ticker.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import okio.ByteString;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpbitTickerService {

  private final ObjectMapper om = new ObjectMapper();
  private final SimpMessagingTemplate messagingTemplate;
  private final RedisUtil redisUtil;

  // 구독할 마켓 목록(쉼표로 구분) 예: "KRW-BTC,KRW-ETH,KRW-XRP"
  @Value("${upbit.markets:KRW-BTC,KRW-ETH}")
  private String markets;

  private OkHttpClient client;
  private WebSocket webSocket;

  private static final String UPBIT_WS_URL = "wss://api.upbit.com/websocket/v1";
  private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

  @PostConstruct
  public void connect() {
    client =
        new OkHttpClient.Builder()
            .pingInterval(15, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS) // stream
            .build();

    Request request = new Request.Builder().url(UPBIT_WS_URL).build();
    webSocket =
        client.newWebSocket(
            request,
            new WebSocketListener() {
              @Override
              public void onOpen(WebSocket webSocket, Response response) {
                log.info("Connected to Upbit WS: {}", response);
                sendSubscribePayload(webSocket);
              }

              @Override
              public void onMessage(WebSocket webSocket, String text) {
                // Upbit는 바이너리로도 내려오지만, 혹시 텍스트 오면 방어
                handleMessage(text.getBytes());
              }

              @Override
              public void onMessage(WebSocket webSocket, ByteString bytes) {
                handleMessage(bytes.toByteArray());
              }

              @Override
              public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                log.error("Upbit WS failure", t);
                reconnect();
              }

              @Override
              public void onClosed(WebSocket webSocket, int code, String reason) {
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
    // 간단 재연결 백오프
    try {
      Thread.sleep(2000L);
    } catch (InterruptedException ignored) {
    }
    connect();
  }

  private void sendSubscribePayload(WebSocket ws) {
    try {
      // Upbit 구독 포맷: [ {ticket}, {type:"ticker","codes":[...]} ]
      var ticket = Map.of("ticket", "tomolow-" + System.currentTimeMillis());
      var codeList =
          Stream.of(markets.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
      var tickerReq = Map.of("type", "ticker", "codes", codeList);
      String payload = om.writeValueAsString(List.of(ticket, tickerReq));
      ws.send(payload);
      log.info("Subscribed markets: {}", codeList);
    } catch (Exception e) {
      log.error("Subscribe payload build error", e);
    }
  }

  private void handleMessage(byte[] raw) {
    try {
      // 바이너리 → Map
      Map<String, Object> m = om.readValue(raw, new TypeReference<>() {});
      // 필요한 필드 추출 (Upbit ticker 필드명 기준)
      String market = (String) m.get("code"); // ex) "KRW-BTC"
      BigDecimal tradePrice = toBig(m.get("trade_price")); // 현재가
      BigDecimal signedChangeRate = toBig(m.get("signed_change_rate")); // 등락률(부호 포함, 예: 0.0123)
      BigDecimal accTradeVolume24h = toBig(m.get("acc_trade_volume_24h")); // 24h 거래량(수량)
      long ts = ((Number) m.get("timestamp")).longValue();

      TickerMessage dto =
          TickerMessage.builder()
              .market(market)
              .tradePrice(tradePrice)
              .changeRate(signedChangeRate)
              .accVolume(accTradeVolume24h)
              .tradeTimestamp(ts)
              .build();

      // 1) Redis 캐시 업데이트 (선택: 문자열 전체/가격만)
      redisUtil.setData("last_price:" + market, tradePrice.toPlainString());
      redisUtil.setData("ticker:" + market, om.writeValueAsString(dto));

      // 2) STOMP 방송
      messagingTemplate.convertAndSend("/topic/ticker/" + market, dto);

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
}
