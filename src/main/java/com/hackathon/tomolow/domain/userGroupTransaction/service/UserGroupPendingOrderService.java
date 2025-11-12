package com.hackathon.tomolow.domain.userGroupTransaction.service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.market.exception.MarketErrorCode;
import com.hackathon.tomolow.domain.market.repository.MarketRepository;
import com.hackathon.tomolow.domain.ticker.service.PriceQueryService;
import com.hackathon.tomolow.domain.transaction.dto.PendingOrderModifyRequestDto;
import com.hackathon.tomolow.domain.transaction.entity.TradeType;
import com.hackathon.tomolow.domain.user.repository.UserRepository;
import com.hackathon.tomolow.domain.userGroup.entity.UserGroup;
import com.hackathon.tomolow.domain.userGroupTransaction.dto.UserGroupPendingOrderDto;
import com.hackathon.tomolow.domain.userGroupTransaction.exception.UserGroupTransactionErrorCode;
import com.hackathon.tomolow.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserGroupPendingOrderService {

  private final GroupOrderInfoService groupOrderInfoService;
  private final GroupOrderRedisService groupOrderRedisService;
  private final MarketRepository marketRepository;
  private final UserRepository userRepository;
  private final PriceQueryService priceQueryService;
  private final UserGroupOrderMatchService userGroupOrderMatchService;

  /** UserGroup의 대기주문 전체 조회 */
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

  /** 대기주문 수정을 위한 시장가 조회 */
  public BigDecimal getLatestMarketPrice(String orderId, Long groupId) {
    String marketId = groupOrderRedisService.getOrderMarketId(orderId, groupId.toString());
    if (marketId == null)
      throw new CustomException(UserGroupTransactionErrorCode.PENDING_ORDER_NOT_EXIST);

    Market market =
        marketRepository
            .findById(Long.valueOf(marketId))
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));
    return priceQueryService.getLastTradePriceOrThrow(market.getSymbol());
  }

  /** 대기주문 수정 */
  public void modifyPendingOrder(
      Long userId, Long groupId, PendingOrderModifyRequestDto modifyRequestDto) {

    String orderId = modifyRequestDto.getOrderId();
    BigDecimal price = modifyRequestDto.getPrice();

    // 1. orderId와 groupId를 통해 대기주문에서 marketId 가져오기
    String marketId = groupOrderRedisService.getOrderMarketId(orderId, groupId.toString());
    if (marketId == null)
      throw new CustomException(UserGroupTransactionErrorCode.PENDING_ORDER_NOT_EXIST);

    Market market =
        marketRepository
            .findById(Long.valueOf(marketId))
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));

    UserGroup userGroup = groupOrderInfoService.getUserGroup(userId, groupId);

    TradeType tradeType = groupOrderRedisService.getTradeType(orderId, groupId.toString());
    if (tradeType == null) throw new CustomException(UserGroupTransactionErrorCode.TRADE_TYPE_NULL);

    // 2. 매수 주문 - ( 갱신할 가격 * 수량 ) > user의 현금 자산인 경우 수정 불가
    if (tradeType == TradeType.BUY) {
      int remainingQuantity =
          groupOrderRedisService.getRemainingQuantity(orderId, groupId.toString());
      BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(remainingQuantity));
      if (totalPrice.compareTo(userGroup.getCashBalance()) > 0)
        throw new CustomException(
            UserGroupTransactionErrorCode.INSUFFICIENT_BALANCE, "잔액이 부족해서 수정할 수 없습니다.");
    }

    // 3. order book (ZSET) 갱신
    groupOrderRedisService.updateOrderBook(orderId, marketId, tradeType, price, groupId.toString());

    // 4. detail (HASH) 갱신
    groupOrderRedisService.updatePrice(orderId, price, groupId.toString());

    // 5. 매칭 시도
    BigDecimal marketPrice = priceQueryService.getLastTradePriceOrThrow(market.getSymbol());
    userGroupOrderMatchService.matchByMarketPrice(marketId, groupId.toString(), marketPrice);
  }
}
