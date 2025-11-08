package com.hackathon.tomolow.domain.market.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.tomolow.domain.market.dto.RankItem;
import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.market.repository.MarketRepository;
import com.hackathon.tomolow.domain.ticker.dto.TickerMessage;
import com.hackathon.tomolow.domain.userInterestedMarket.entity.UserInterestedMarket;
import com.hackathon.tomolow.domain.userInterestedMarket.repository.UserInterestedMarketRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RankingService {

  private final StringRedisTemplate redis;
  private final ObjectMapper om;
  private final SimpMessagingTemplate broker;
  private final MarketRepository marketRepository;
  private final UserInterestedMarketRepository interestedRepo;

  private static final String Z_TURNOVER = "rank:turnover"; // 거래대금
  private static final String Z_VOLUME = "rank:volume"; // 거래량
  private static final String Z_GAINERS = "rank:gainers"; // 급상승
  private static final String Z_LOSERS = "rank:losers"; // 급하락

  // ============ 점수 업데이트 ============
  public void onTick(TickerMessage t) {
    String sym = t.getMarket();
    double sTurnover = nz(t.getAccTradePrice24h());
    double sVolume = nz(t.getAccVolume());
    double sGainers = nz(t.getChangeRate()); // changeRate 높을수록 상위
    double sLosers = -nz(t.getChangeRate()); // 하락폭 큰(음수 절대값 큼) 것 상위

    redis.opsForZSet().add(Z_TURNOVER, sym, sTurnover);
    redis.opsForZSet().add(Z_VOLUME, sym, sVolume);
    redis.opsForZSet().add(Z_GAINERS, sym, sGainers);
    redis.opsForZSet().add(Z_LOSERS, sym, sLosers);
  }

  private double nz(BigDecimal v) {
    return (v == null ? 0d : v.doubleValue());
  }

  // ============ 공용: TOP N (관심여부 없음) ============
  @Transactional(readOnly = true)
  public List<RankItem> getTopPublic(String type, int limit) {
    String key = pickKey(type);
    Set<String> syms = redis.opsForZSet().reverseRange(key, 0, limit - 1);
    if (syms == null || syms.isEmpty()) return List.of();

    // 심볼→마켓 캐시
    Map<String, Market> marketMap =
        marketRepository.findAllBySymbolIn(syms).stream()
            .collect(Collectors.toMap(Market::getSymbol, m -> m));

    return syms.stream().map(sym -> toRankItem(sym, marketMap, null)).toList();
  }

  // ============ 개인화: TOP N (관심여부 포함) ============
  @Transactional(readOnly = true)
  public List<RankItem> getTopWithInterest(String type, int limit, Long userId) {
    String key = pickKey(type);
    Set<String> syms = redis.opsForZSet().reverseRange(key, 0, limit - 1);
    if (syms == null || syms.isEmpty()) return List.of();

    Map<String, Market> marketMap =
        marketRepository.findAllBySymbolIn(syms).stream()
            .collect(Collectors.toMap(Market::getSymbol, m -> m));

    // 유저 관심 마켓 id → boolean map
    Map<Long, Boolean> interestedMap =
        (userId == null)
            ? Map.of()
            : interestedRepo.findAllByUser_Id(userId).stream()
                .map(UserInterestedMarket::getMarket)
                .collect(Collectors.toMap(Market::getId, m -> Boolean.TRUE, (a, b) -> a));

    return syms.stream().map(sym -> toRankItem(sym, marketMap, interestedMap)).toList();
  }

  private RankItem toRankItem(
      String symbol, Map<String, Market> marketMap, Map<Long, Boolean> interestedMapOrNull) {
    try {
      String json = redis.opsForValue().get("ticker:" + symbol);
      if (json == null) {
        Market m = marketMap.get(symbol);
        return RankItem.builder()
            .symbol(symbol)
            .name(m != null ? m.getName() : symbol)
            .imageUrl(m != null ? m.getImgUrl() : null)
            .price(BigDecimal.ZERO)
            .build();
      }
      var t = om.readTree(json); // 가볍게 읽기
      Market m = marketMap.get(symbol);

      Boolean interested = null;
      if (interestedMapOrNull != null && m != null) {
        interested = interestedMapOrNull.getOrDefault(m.getId(), Boolean.FALSE);
      }

      return RankItem.builder()
          .symbol(symbol)
          .name(m != null ? m.getName() : symbol)
          .imageUrl(m != null ? m.getImgUrl() : null)
          .price(readBig(t, "tradePrice"))
          .changeRate(readBig(t, "changeRate"))
          .changePrice(readBig(t, "changePrice"))
          .interested(interested) // 🔸 REST 초기 1회에서만 세팅, STOMP는 null
          .build();
    } catch (Exception e) {
      Market m = marketMap.get(symbol);
      return RankItem.builder()
          .symbol(symbol)
          .name(m != null ? m.getName() : symbol)
          .imageUrl(m != null ? m.getImgUrl() : null)
          .price(BigDecimal.ZERO)
          .build();
    }
  }

  private BigDecimal readBig(com.fasterxml.jackson.databind.JsonNode n, String field) {
    var v = n.get(field);
    return (v == null || v.isNull()) ? BigDecimal.ZERO : v.decimalValue();
  }

  private String pickKey(String type) {
    return switch (type) {
      case "turnover" -> Z_TURNOVER;
      case "volume" -> Z_VOLUME;
      case "gainers" -> Z_GAINERS;
      case "losers" -> Z_LOSERS;
      default -> Z_TURNOVER;
    };
  }

  // ============ 1초 코얼레싱 푸시(공용) ============
  private volatile String lastTurnoverPayload = "";
  private volatile String lastVolumePayload = "";
  private volatile String lastGainersPayload = "";
  private volatile String lastLosersPayload = "";

  @Scheduled(fixedDelay = 1000L)
  public void pushTopEverySec() {
    pushIfChanged(
        "/topic/rank/turnover",
        lastTurnoverPayload,
        p -> lastTurnoverPayload = p,
        getTopPublic("turnover", 50));
    pushIfChanged(
        "/topic/rank/volume",
        lastVolumePayload,
        p -> lastVolumePayload = p,
        getTopPublic("volume", 50));
    pushIfChanged(
        "/topic/rank/gainers",
        lastGainersPayload,
        p -> lastGainersPayload = p,
        getTopPublic("gainers", 50));
    pushIfChanged(
        "/topic/rank/losers",
        lastLosersPayload,
        p -> lastLosersPayload = p,
        getTopPublic("losers", 50));
  }

  private void pushIfChanged(
      String topic, String last, Consumer<String> setLast, List<RankItem> list) {
    try {
      String payload = om.writeValueAsString(list);
      if (!payload.equals(last)) {
        setLast.accept(payload);
        broker.convertAndSend(topic, payload);
      }
    } catch (Exception ignored) {
    }
  }
}
