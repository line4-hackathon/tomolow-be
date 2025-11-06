package com.hackathon.tomolow.domain.transaction.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.market.exception.MarketErrorCode;
import com.hackathon.tomolow.domain.market.repository.MarketRepository;
import com.hackathon.tomolow.domain.transaction.entity.TradeType;
import com.hackathon.tomolow.domain.transaction.entity.Transaction;
import com.hackathon.tomolow.domain.transaction.repository.TransactionRepository;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.user.exception.UserErrorCode;
import com.hackathon.tomolow.domain.user.repository.UserRepository;
import com.hackathon.tomolow.domain.userMarketHolding.entity.UserMarketHolding;
import com.hackathon.tomolow.domain.userMarketHolding.exception.UserMarketHoldingErrorCode;
import com.hackathon.tomolow.domain.userMarketHolding.repository.UserMarketHoldingRepository;
import com.hackathon.tomolow.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

  private final OrderRedisService orderRedisService;
  private final MarketRepository marketRepository;
  private final UserRepository userRepository;
  private final TransactionRepository transactionRepository;
  private final UserMarketHoldingRepository userMarketHoldingRepository;

  /** 실시간 가격 기반 지정가 체결 */
  @Transactional
  public void matchByMarketPrice(String marketId, BigDecimal marketPrice) {

    // 지정가 매수 : 시장가 <= 지정가 -> 체결
    List<String> buyOrders = orderRedisService.findBuyOrderAtOrAbovePrice(marketId, marketPrice);
    for (String buyOrderId : buyOrders) {
      int remaining = orderRedisService.getRemainingQuantity(buyOrderId);
      if (remaining <= 0) continue;

      BigDecimal limitPrice = orderRedisService.getPrice(buyOrderId);
      if (marketPrice.compareTo(limitPrice) <= 0)
        executeBuy(marketId, buyOrderId, limitPrice, remaining);
    }

    // 지정가 매도 : 시장가 >= 지정가 -> 체결
    List<String> sellOrders = orderRedisService.findSellOrderAtOrBelowPrice(marketId, marketPrice);
    for (String sellOrderId : sellOrders) {
      int remaining = orderRedisService.getRemainingQuantity(sellOrderId);
      if (remaining <= 0) continue;

      BigDecimal limitPrice = orderRedisService.getPrice(sellOrderId);
      if (marketPrice.compareTo(limitPrice) >= 0)
        executeSell(marketId, sellOrderId, limitPrice, remaining);
    }
  }

  private void executeBuy(String marketId, String orderId, BigDecimal tradePrice, int quantity) {

    Market market =
        marketRepository
            .findById(Long.valueOf(marketId))
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));

    User user =
        userRepository
            .findById(Long.valueOf(orderRedisService.getUserId(orderId)))
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    BigDecimal totalCost = tradePrice.multiply(BigDecimal.valueOf(quantity));

    // 1. 잔고 부족 시 주문 취소
    if (user.getCashBalance().compareTo(totalCost) < 0) {
      orderRedisService.removeOrder(marketId, TradeType.BUY, orderId);
      return;
    }

    // 2. 사용자 자산 변화
    user.subtractCashBalance(totalCost);
    user.addInvestmentBalance(totalCost);

    // 3. 사용자 주식 보유 수량 증가
    UserMarketHolding userMarketHolding =
        userMarketHoldingRepository
            .findByUserAndMarket(user, market)
            .orElseGet(
                () -> {
                  UserMarketHolding newBuyUserMarketHolding =
                      UserMarketHolding.builder()
                          .market(market)
                          .user(user)
                          .quantity(0L)
                          .avgPrice(BigDecimal.ZERO)
                          .build();
                  return userMarketHoldingRepository.save(newBuyUserMarketHolding);
                });
    userMarketHolding.addQuantity(quantity, tradePrice);

    // 4. 잔여 수량 갱신
    orderRedisService.updateOrRemove(orderId, marketId, TradeType.BUY, quantity);

    // 5. 체결 내역 DB에 저장
    Transaction transaction =
        Transaction.builder()
            .market(market)
            .user(user)
            .price(tradePrice)
            .quantity(quantity)
            .tradeType(TradeType.BUY)
            .build();
    transactionRepository.save(transaction);

    log.info("매수 체결 - orderId : " + orderId);
  }

  private void executeSell(String marketId, String orderId, BigDecimal tradePrice, int quantity) {
    Market market =
        marketRepository
            .findById(Long.valueOf(marketId))
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));
    User user =
        userRepository
            .findById(Long.valueOf(orderRedisService.getUserId(orderId)))
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    UserMarketHolding holding =
        userMarketHoldingRepository
            .findByUserAndMarket(user, market)
            .orElseThrow(
                () ->
                    new CustomException(
                        UserMarketHoldingErrorCode.HOLDING_NOT_FOUND,
                        "사용자가 해당 market을 보유하고 있지 않습니다"));

    // 1. 수량 부족 시 주문 취소
    if (holding.getQuantity() < quantity) {
      orderRedisService.removeOrder(marketId, TradeType.SELL, orderId);
      return;
    }

    // 2. 보유 수량 감소
    holding.subtractQuantity(quantity);

    // 3. 사용자 자산 변화
    BigDecimal totalPrice = tradePrice.multiply(BigDecimal.valueOf(quantity));
    user.addCashBalance(totalPrice);
    user.subtractInvestmentBalance(totalPrice);

    // 4. 잔여 수량 갱신
    orderRedisService.updateOrRemove(orderId, marketId, TradeType.SELL, quantity);

    // 5. 체결 내역 DB에 저장
    Transaction transaction =
        Transaction.builder()
            .market(market)
            .user(user)
            .price(tradePrice)
            .quantity(quantity)
            .tradeType(TradeType.SELL)
            .build();
    transactionRepository.save(transaction);

    log.info("매도 체결 - orderId : " + orderId);
  }
}
