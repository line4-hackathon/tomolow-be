package com.hackathon.tomolow.domain.transaction.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.market.exception.MarketErrorCode;
import com.hackathon.tomolow.domain.market.repository.MarketRepository;
import com.hackathon.tomolow.domain.transaction.dto.OrderRequestDto;
import com.hackathon.tomolow.domain.transaction.entity.TradeType;
import com.hackathon.tomolow.domain.transaction.entity.Transaction;
import com.hackathon.tomolow.domain.transaction.exception.TransactionErrorCode;
import com.hackathon.tomolow.domain.transaction.repository.TransactionRepository;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.user.exception.UserErrorCode;
import com.hackathon.tomolow.domain.user.repository.UserRepository;
import com.hackathon.tomolow.domain.userMarketHolding.entity.UserMarketHolding;
import com.hackathon.tomolow.domain.userMarketHolding.exception.UserMarketHoldingErrorCode;
import com.hackathon.tomolow.domain.userMarketHolding.repository.UserMarketHoldingRepository;
import com.hackathon.tomolow.global.exception.CustomException;
import com.hackathon.tomolow.global.security.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MarketTransactionService {

  private final MarketRepository marketRepository;
  private final UserRepository userRepository;
  private final SecurityUtil securityUtil;
  private final TransactionRepository transactionRepository;
  private final UserMarketHoldingRepository userMarketHoldingRepository;

  /** 시장가 매수 */
  @Transactional
  public void marketBuy(Long marketId, OrderRequestDto orderRequestDto) {
    Market market =
        marketRepository
            .findById(marketId)
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));

    Long currentUserId = securityUtil.getCurrentUserId();
    User user =
        userRepository
            .findById(currentUserId)
            .orElseThrow(
                () -> new CustomException(UserErrorCode.USER_NOT_FOUND, "해당 id의 유저가 존재하지 않습니다."));

    BigDecimal price = orderRequestDto.getPrice();
    int quantity = orderRequestDto.getQuantity();

    // 1. 매수 가능 잔고인지 확인
    BigDecimal totalCost = price.multiply(BigDecimal.valueOf(quantity));
    if (user.getCashBalance().compareTo(totalCost) < 0) {
      throw new CustomException(TransactionErrorCode.INSUFFICIENT_BALANCE);
    }

    // 2. 사용자 자산 변화
    user.subtractCashBalance(totalCost);
    user.addInvestmentBalance(totalCost);

    // 3. 보유 수량 갱신
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
    userMarketHolding.addQuantity(quantity, price);

    // 4. 거래 내역 DB에 저장
    Transaction transaction =
        Transaction.builder()
            .tradeType(TradeType.BUY)
            .quantity(quantity)
            .price(price)
            .user(user)
            .market(market)
            .build();
    transactionRepository.save(transaction);

    System.out.println("시장가 매수");
  }

  /** 시장가 매도 */
  public void marketSell(Long marketId, OrderRequestDto orderRequestDto) {
    Market market =
        marketRepository
            .findById(marketId)
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));

    Long currentUserId = securityUtil.getCurrentUserId();
    User user =
        userRepository
            .findById(currentUserId)
            .orElseThrow(
                () -> new CustomException(UserErrorCode.USER_NOT_FOUND, "해당 id의 유저가 존재하지 않습니다."));

    BigDecimal price = orderRequestDto.getPrice();
    int quantity = orderRequestDto.getQuantity();

    // 1. 매도 가능한 수량을 가지고 있는지 확인
    UserMarketHolding userMarketHolding =
        userMarketHoldingRepository
            .findByUserAndMarket(user, market)
            .orElseThrow(
                () ->
                    new CustomException(
                        UserMarketHoldingErrorCode.HOLDING_NOT_FOUND, "유저가 해당 종목을 보유하고 있지 않습니다."));
    if (userMarketHolding.getQuantity() < orderRequestDto.getQuantity()) {
      throw new CustomException(UserMarketHoldingErrorCode.INSUFFICIENT_QUANTITY);
    }

    // 2. 보유 수량 감소
    userMarketHolding.subtractQuantity(quantity);

    // 3. 사용자 자산 변화
    BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(quantity));
    user.addCashBalance(totalPrice);
    user.subtractInvestmentBalance(totalPrice);

    // 4. 체결 내역 DB에 저장
    Transaction transaction =
        Transaction.builder()
            .market(market)
            .user(user)
            .price(price)
            .quantity(quantity)
            .tradeType(TradeType.SELL)
            .build();
    transactionRepository.save(transaction);

    System.out.println("시장가 매도");
  }
}
