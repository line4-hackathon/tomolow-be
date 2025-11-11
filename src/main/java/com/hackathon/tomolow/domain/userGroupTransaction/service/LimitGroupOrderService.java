package com.hackathon.tomolow.domain.userGroupTransaction.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.market.exception.MarketErrorCode;
import com.hackathon.tomolow.domain.market.repository.MarketRepository;
import com.hackathon.tomolow.domain.ticker.service.PriceQueryService;
import com.hackathon.tomolow.domain.transaction.dto.OrderRequestDto;
import com.hackathon.tomolow.domain.transaction.entity.TradeType;
import com.hackathon.tomolow.domain.userGroup.entity.UserGroup;
import com.hackathon.tomolow.domain.userGroupStockHolding.entity.UserGroupMarketHolding;
import com.hackathon.tomolow.domain.userGroupStockHolding.exception.UserGroupMarketHoldingErrorCode;
import com.hackathon.tomolow.domain.userGroupStockHolding.repository.UserGroupMarketHoldingRepository;
import com.hackathon.tomolow.domain.userGroupTransaction.exception.UserGroupTransactionErrorCode;
import com.hackathon.tomolow.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LimitGroupOrderService {

  private final MarketRepository marketRepository;
  private final GroupOrderInfoService groupOrderInfoService;
  private final PriceQueryService priceQueryService;
  private final GroupOrderRedisService groupOrderRedisService;
  private final UserGroupMarketHoldingRepository userGroupMarketHoldingRepository;
  private final UserGroupOrderMatchService userGroupOrderMatchService;

  /** 지정가 매수 */
  @Transactional
  public String limitBuy(
      Long userId, Long groupId, Long marketId, OrderRequestDto orderRequestDto) {
    Market market =
        marketRepository
            .findById(marketId)
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));

    // 1. UserGroup 불러오기
    UserGroup userGroup = groupOrderInfoService.getUserGroup(userId, groupId);

    // 2. 현재 시장가 불러오기
    BigDecimal marketPrice = priceQueryService.getLastTradePriceOrThrow(market.getSymbol());

    // 3. 매수 가능 잔고인지 확인
    BigDecimal totalCost =
        orderRequestDto.getPrice().multiply(BigDecimal.valueOf(orderRequestDto.getQuantity()));
    if (userGroup.getCashBalance().compareTo(totalCost) < 0) {
      throw new CustomException(UserGroupTransactionErrorCode.INSUFFICIENT_BALANCE);
    }

    // 4. 주문 ID 생성
    String orderId = groupId.toString() + userId.toString() + UUID.randomUUID();

    // 5. Redis 내 대기 주문 저장
    groupOrderRedisService.saveOrder(
        market.getId().toString(),
        orderId,
        TradeType.BUY,
        orderRequestDto.getPrice(),
        orderRequestDto.getQuantity(),
        userId.toString(),
        groupId.toString());

    // 6. 매칭 시도
    userGroupOrderMatchService.matchByMarketPrice(
        String.valueOf(marketId), String.valueOf(groupId), marketPrice);

    return orderId;
  }

  /** 지정가 매도 */
  @Transactional
  public String limitSell(
      Long userId, Long groupId, Long marketId, OrderRequestDto orderRequestDto) {
    Market market =
        marketRepository
            .findById(marketId)
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));

    // 1. UserGroup 불러오기
    UserGroup userGroup = groupOrderInfoService.getUserGroup(userId, groupId);

    // 2. 현재 시장가 불러오기
    BigDecimal marketPrice = priceQueryService.getLastTradePriceOrThrow(market.getSymbol());

    // 3. 매도 가능한 수량을 가지고 있는지 확인
    UserGroupMarketHolding userGroupMarketHolding =
        userGroupMarketHoldingRepository
            .findByUserGroup_IdAndMarket_Id(userGroup.getId(), marketId)
            .orElseThrow(
                () ->
                    new CustomException(
                        UserGroupMarketHoldingErrorCode.HOLDING_NOT_FOUND,
                        "유저가 해당 종목을 보유하고 있지 않습니다."));
    if (userGroupMarketHolding.getQuantity() < orderRequestDto.getQuantity()) {
      throw new CustomException(UserGroupMarketHoldingErrorCode.INSUFFICIENT_QUANTITY);
    }

    // 4. 주문 ID 생성
    String orderId = groupId.toString() + userId.toString() + UUID.randomUUID();

    // 5. Redis 내 대기 주문 저장
    groupOrderRedisService.saveOrder(
        market.getId().toString(),
        orderId,
        TradeType.SELL,
        orderRequestDto.getPrice(),
        orderRequestDto.getQuantity(),
        userId.toString(),
        groupId.toString());

    // 6. 매칭 시도
    userGroupOrderMatchService.matchByMarketPrice(
        String.valueOf(marketId), String.valueOf(groupId), marketPrice);

    return orderId;
  }
}
