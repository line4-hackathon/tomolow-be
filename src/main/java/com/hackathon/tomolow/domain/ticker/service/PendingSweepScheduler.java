package com.hackathon.tomolow.domain.ticker.service;

import java.math.BigDecimal;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hackathon.tomolow.domain.market.repository.MarketRepository;
import com.hackathon.tomolow.domain.transaction.service.MatchService;
import com.hackathon.tomolow.domain.transaction.service.OrderRedisService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PendingSweepScheduler {

  private final OrderRedisService orderRedisService;
  private final MatchService matchService;
  private final PriceQueryService priceQueryService;
  private final MarketRepository marketRepository;

  @Scheduled(fixedDelay = 10_000L) // 10초마다
  public void sweep() {
    // 1) Redis에서 "주문 남아있는 marketId"만 가져오기
    var pendingMarketIds = orderRedisService.getPendingMarketIds();
    if (pendingMarketIds.isEmpty()) {
      return;
    }

    // 2) 배치로 Market 조회 후 (id -> Market) 매핑
    var idList = pendingMarketIds.stream().map(Long::valueOf).toList();
    var marketMap =
        marketRepository.findAllById(idList).stream()
            .collect(java.util.stream.Collectors.toMap(m -> m.getId(), m -> m));

    // 3) 각 마켓의 심볼로 현재가 조회 → 매칭 시도
    for (String marketId : pendingMarketIds) {
      var m = marketMap.get(Long.valueOf(marketId));
      if (m == null) {
        continue; // 방어
      }

      try {
        BigDecimal last = priceQueryService.getLastTradePriceOrThrow(m.getSymbol());
        matchService.matchByMarketPrice(marketId, last);
      } catch (Exception e) {
        // 개별 마켓 실패는 건너뛰고 다음으로
        // log.warn("sweep fail for marketId={}, symbol={}", marketId, m.getSymbol(), e);
      }
    }
  }
}
