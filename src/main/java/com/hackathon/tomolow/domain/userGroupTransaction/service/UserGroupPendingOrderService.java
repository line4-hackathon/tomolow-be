package com.hackathon.tomolow.domain.userGroupTransaction.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.market.repository.MarketRepository;
import com.hackathon.tomolow.domain.transaction.entity.TradeType;
import com.hackathon.tomolow.domain.userGroup.entity.UserGroup;
import com.hackathon.tomolow.domain.userGroupTransaction.dto.UserGroupPendingOrderDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserGroupPendingOrderService {

  private final GroupOrderInfoService groupOrderInfoService;
  private final GroupOrderRedisService groupOrderRedisService;
  private final MarketRepository marketRepository;

  public List<UserGroupPendingOrderDto> getUserGroupPendingOrders(Long userId, Long groupId) {
    // 1. 해당 id의 그룹, 사용자가 존재하는지 체크
    UserGroup userGroup = groupOrderInfoService.getUserGroup(userId, groupId);

    // 2. UserGroup의 대기주문 ID 전체 조회
    Set<String> orderIds =
        groupOrderRedisService.listUserGroupOpenOrderIds(userId.toString(), groupId.toString());

    // 3. 잔량 > 0인 주문만 필터링
    List<String> pendingOrderIds =
        orderIds.stream()
            .filter(oid -> groupOrderRedisService.getRemainingQuantity(oid, groupId.toString()) > 0)
            .toList();
    if (pendingOrderIds.isEmpty()) return List.of();

    // 4. 주문 -> 마켓 매핑 (주문이 어떤 종목에 대한 주문인지 매핑)
    Map<String, Long> orderToMarket =
        pendingOrderIds.stream()
            .collect(
                Collectors.toMap(
                    oid -> oid,
                    oid -> {
                      String mid = groupOrderRedisService.getOrderMarketId(oid, groupId.toString());
                      return (mid == null || mid.isBlank()) ? null : Long.valueOf(mid);
                    }))
            .entrySet()
            .stream()
            .filter(e -> e.getValue() != null)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    if (orderToMarket.isEmpty()) return List.of();

    // 5. 마켓 일괄 로드
    Set<Long> marketIds = new HashSet<>(orderToMarket.values());
    Map<Long, Market> marketMap =
        marketRepository.findAllById(marketIds).stream()
            .collect(Collectors.toMap(Market::getId, m -> m));

    // 6. UserGroupPendingOrderDto에 매핑(주문 단위)
    List<UserGroupPendingOrderDto> dtos = new ArrayList<>();
    for (String oid : pendingOrderIds) {
      Long marketId = orderToMarket.get(oid);
      if (marketId == null) continue;
      Market market = marketMap.get(marketId);
      if (market == null) continue;

      int remaining = groupOrderRedisService.getRemainingQuantity(oid, groupId.toString());
      if (remaining <= 0) continue;

      TradeType tradeType = groupOrderRedisService.getTradeType(oid, groupId.toString());

      UserGroupPendingOrderDto dto =
          UserGroupPendingOrderDto.builder()
              .orderId(oid)
              .imageUrl(market.getImgUrl())
              .quantity(remaining)
              .tradeType(tradeType)
              .marketName(market.getName())
              .marketId(marketId)
              .build();
      dtos.add(dto);
    }
    return dtos;
  }
}
