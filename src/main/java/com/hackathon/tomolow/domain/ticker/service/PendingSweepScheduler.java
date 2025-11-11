package com.hackathon.tomolow.domain.ticker.service;

import java.math.BigDecimal;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hackathon.tomolow.domain.market.repository.MarketRepository;
import com.hackathon.tomolow.domain.transaction.service.MatchService;
import com.hackathon.tomolow.domain.transaction.service.OrderRedisService;
import com.hackathon.tomolow.domain.userGroupTransaction.service.GroupOrderRedisService;
import com.hackathon.tomolow.domain.userGroupTransaction.service.UserGroupOrderMatchService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PendingSweepScheduler {

  private final OrderRedisService orderRedisService;
  private final MatchService matchService;
  private final PriceQueryService priceQueryService;
  private final MarketRepository marketRepository;
  private final GroupOrderRedisService groupOrderRedisService;
  private final UserGroupOrderMatchService userGroupOrderMatchService;

  @Scheduled(fixedDelay = 10_000L) // 10초마다
  public void sweep() {
    // 1) Redis에서 "주문 남아있는 marketId"만 가져오기
    // 1-1) 개인 투자
    var pendingMarketIds = orderRedisService.getPendingMarketIds();
    // 1-2) 그룹 투자
    var groupPendingMarketIds = groupOrderRedisService.getPendingMarketIds();
    if (pendingMarketIds.isEmpty() && groupPendingMarketIds.isEmpty()) {
      return;
    }

    // 2) 개인 투자
    if (!pendingMarketIds.isEmpty()) {
      // 2-1) 배치로 Market 조회 후 (id -> Market) 매핑
      var idList = pendingMarketIds.stream().map(Long::valueOf).toList();
      var marketMap =
          marketRepository.findAllById(idList).stream()
              .collect(java.util.stream.Collectors.toMap(m -> m.getId(), m -> m));
      // 2-2) 각 마켓의 심볼로 현재가 조회 → 매칭 시도
      for (String marketId : pendingMarketIds) {
        var m = marketMap.get(Long.valueOf(marketId));
        if (m == null) continue;

        try {
          BigDecimal last = priceQueryService.getLastTradePriceOrThrow(m.getSymbol());
          matchService.matchByMarketPrice(marketId, last);
        } catch (Exception e) {
          // 개별 마켓 실패는 건너뛰고 다음으로
        }
      }
    }
    // 3) 그룹 투자
    if (!groupPendingMarketIds.isEmpty()) {
      // 3-1) 배치로 Market 조회 후 (id -> Market) 매핑
      var idListForGroup = groupPendingMarketIds.stream().map(Long::valueOf).toList();
      var marketMapForGroup =
          marketRepository.findAllById(idListForGroup).stream()
              .collect(java.util.stream.Collectors.toMap(m -> m.getId(), m -> m));
      // 3-2) 각 마켓의 심볼로 현재가 조회 → 매칭 시도
      for (String marketId : groupPendingMarketIds) {
        var m = marketMapForGroup.get(Long.valueOf(marketId));
        if (m == null) continue;

        try {
          BigDecimal last = priceQueryService.getLastTradePriceOrThrow(m.getSymbol());

          // 마켓 기준으로 열린 그룹 id 조회
          var groupIdsWithOpenOrders = groupOrderRedisService.getGroupsWithOpenOrders(marketId);

          for (String groupId : groupIdsWithOpenOrders) {
            userGroupOrderMatchService.matchByMarketPrice(marketId, groupId, last);
          }
        } catch (Exception e) {
          // 개별 마켓 실패는 건너뛰고 다음으로
        }
      }
    }
  }
}
