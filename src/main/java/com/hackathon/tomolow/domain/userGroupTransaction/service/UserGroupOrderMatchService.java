package com.hackathon.tomolow.domain.userGroupTransaction.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.market.exception.MarketErrorCode;
import com.hackathon.tomolow.domain.market.repository.MarketRepository;
import com.hackathon.tomolow.domain.transaction.entity.TradeType;
import com.hackathon.tomolow.domain.userGroup.entity.UserGroup;
import com.hackathon.tomolow.domain.userGroupStockHolding.entity.UserGroupMarketHolding;
import com.hackathon.tomolow.domain.userGroupStockHolding.repository.UserGroupMarketHoldingRepository;
import com.hackathon.tomolow.domain.userGroupTransaction.entity.UserGroupTransaction;
import com.hackathon.tomolow.domain.userGroupTransaction.repository.UserGroupTransactionRepository;
import com.hackathon.tomolow.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserGroupOrderMatchService {

  private final GroupOrderRedisService groupOrderRedisService;
  private final GroupOrderInfoService groupOrderInfoService;
  private final MarketRepository marketRepository;
  private final UserGroupMarketHoldingRepository userGroupMarketHoldingRepository;
  private final UserGroupTransactionRepository userGroupTransactionRepository;

  /** 실시간 가격 기반 지정가 체결 */
  @Transactional
  public void matchByMarketPrice(String marketId, String groupId, BigDecimal marketPrice) {

    // 지정가 매수 : 시장가 <= 지정가 -> 체결
    List<String> buyOrders =
        groupOrderRedisService.findBuyOrderAtOrAbovePrice(marketId, marketPrice, groupId);
    for (String buyOrderId : buyOrders) {
      int remaining = groupOrderRedisService.getRemainingQuantity(buyOrderId, groupId);
      if (remaining <= 0) {
        continue;
      }

      BigDecimal limitPrice = groupOrderRedisService.getPrice(buyOrderId, groupId);
      if (marketPrice.compareTo(limitPrice) <= 0) {
        executeBuy(marketId, groupId, buyOrderId, limitPrice, remaining);
      }
    }

    // 지정가 매도 : 시장가 >= 지정가 -> 체결
    List<String> sellOrders =
        groupOrderRedisService.findSellOrderAtOrBelowPrice(marketId, marketPrice, groupId);
    for (String sellOrderId : sellOrders) {
      int remaining = groupOrderRedisService.getRemainingQuantity(sellOrderId, groupId);
      if (remaining <= 0) {
        continue;
      }

      BigDecimal limitPrice = groupOrderRedisService.getPrice(sellOrderId, groupId);
      if (marketPrice.compareTo(limitPrice) >= 0) {
        executeSell(marketId, groupId, sellOrderId, limitPrice, remaining);
      }
    }
  }

  private void executeBuy(
      String marketId, String groupId, String orderId, BigDecimal tradePrice, int quantity) {

    Market market =
        marketRepository
            .findById(Long.valueOf(marketId))
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));

    // 1. Redis 정보 기반으로 UserGroup 조회
    UserGroup userGroup =
        groupOrderInfoService.getUserGroup(
            Long.valueOf(groupOrderRedisService.getUserId(orderId, groupId)),
            Long.valueOf(groupId));

    // 2. 잔고 부족 시 주문 취소
    BigDecimal totalCost = tradePrice.multiply(BigDecimal.valueOf(quantity));

    if (userGroup.getCashBalance().compareTo(totalCost) < 0) {
      groupOrderRedisService.removeOrder(marketId, TradeType.BUY, orderId, groupId);
      return;
    }

    // 3. 그룹 내 사용자 자산 변화
    userGroup.subtractCash(totalCost);
    userGroup.addInvestment(totalCost);

    // 4. 사용자 주식 보유 수량 증가
    UserGroupMarketHolding userGroupMarketHolding =
        userGroupMarketHoldingRepository
            .findByUserGroupAndMarket(userGroup, market)
            .orElseGet(
                () -> {
                  UserGroupMarketHolding newBuyUserGroupMarketHolding =
                      UserGroupMarketHolding.builder()
                          .market(market)
                          .userGroup(userGroup)
                          .quantity(0L)
                          .avgPrice(BigDecimal.ZERO)
                          .build();
                  return userGroupMarketHoldingRepository.save(newBuyUserGroupMarketHolding);
                });
    userGroupMarketHolding.addQuantity(quantity, tradePrice);

    // 5. 잔여 수량 갱신
    groupOrderRedisService.updateOrRemove(orderId, marketId, TradeType.BUY, quantity, groupId);

    // 6. 체결 내역 DB에 저장
    UserGroupTransaction transaction =
        UserGroupTransaction.builder()
            .market(market)
            .userGroup(userGroup)
            .price(tradePrice)
            .quantity(quantity)
            .tradeType(TradeType.BUY)
            .build();
    userGroupTransactionRepository.save(transaction);

    log.info("그룹 매수 체결 - orderId : " + orderId);
  }

  @Transactional
  public void executeSell(
      String marketId, String groupId, String orderId, BigDecimal tradePrice, int quantity) {
    Market market =
        marketRepository
            .findById(Long.valueOf(marketId))
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));

    UserGroup userGroup =
        groupOrderInfoService.getUserGroup(
            Long.valueOf(groupOrderRedisService.getUserId(orderId, groupId)),
            Long.valueOf(groupId));

    UserGroupMarketHolding holding =
        userGroupMarketHoldingRepository
            .findByUserGroupAndMarket(userGroup, market)
            .orElseGet(
                () -> {
                  groupOrderRedisService.removeOrder(marketId, TradeType.SELL, orderId, groupId);
                  return null;
                });

    if (holding == null) return;

    // 1. 수량 부족 시 주문 취소
    if (holding.getQuantity() < quantity) {
      groupOrderRedisService.removeOrder(marketId, TradeType.SELL, orderId, groupId);
      return;
    }

    // 2. 보유 수량 감소
    holding.subtractQuantity(quantity);

    // 3. 사용자 자산 변화
    BigDecimal totalPrice = tradePrice.multiply(BigDecimal.valueOf(quantity)); // 매도금액
    userGroup.addCash(totalPrice);

    BigDecimal costBasis = holding.getAvgPrice().multiply(BigDecimal.valueOf(quantity));
    userGroup.subtractInvestment(costBasis);

    // 4. 잔여 수량 갱신
    groupOrderRedisService.updateOrRemove(orderId, marketId, TradeType.SELL, quantity, groupId);
    if (holding.getQuantity() <= 0) userGroupMarketHoldingRepository.delete(holding);

    // 5. 체결 내역 DB에 저장
    UserGroupTransaction transaction =
        UserGroupTransaction.builder()
            .market(market)
            .userGroup(userGroup)
            .price(tradePrice)
            .quantity(quantity)
            .tradeType(TradeType.SELL)
            .build();
    userGroupTransactionRepository.save(transaction);

    log.info("그룹 매도 체결 - orderId : " + orderId);
  }
}
