package com.hackathon.tomolow.domain.transaction.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.market.exception.MarketErrorCode;
import com.hackathon.tomolow.domain.market.repository.MarketRepository;
import com.hackathon.tomolow.domain.ticker.service.PriceQueryService;
import com.hackathon.tomolow.domain.transaction.dto.OrderRequestDto;
import com.hackathon.tomolow.domain.transaction.entity.TradeType;
import com.hackathon.tomolow.domain.transaction.exception.TransactionErrorCode;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.user.exception.UserErrorCode;
import com.hackathon.tomolow.domain.user.repository.UserRepository;
import com.hackathon.tomolow.domain.userMarketHolding.entity.UserMarketHolding;
import com.hackathon.tomolow.domain.userMarketHolding.exception.UserMarketHoldingErrorCode;
import com.hackathon.tomolow.domain.userMarketHolding.repository.UserMarketHoldingRepository;
import com.hackathon.tomolow.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LimitTransactionService {

  private final MarketRepository marketRepository;
  private final UserRepository userRepository;
  private final OrderRedisService orderRedisService;
  private final MatchService matchService;
  private final UserMarketHoldingRepository userMarketHoldingRepository;
  private final PriceQueryService priceQueryService;

  /** 지정가 매수 */
  @Transactional
  public String limitBuy(Long userId, Long marketId, OrderRequestDto orderRequestDto) {
    Market market =
        marketRepository
            .findById(marketId)
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> new CustomException(UserErrorCode.USER_NOT_FOUND, "해당 id의 유저가 존재하지 않습니다."));

    // TODO : 현재 시장가 불러오기
    // ✅ 현재 시장가 (실시간)
    BigDecimal marketPrice = priceQueryService.getLastTradePriceOrThrow(market.getSymbol());

    // 매수 가능 잔고인지 확인
    BigDecimal totalCost =
        orderRequestDto.getPrice().multiply(BigDecimal.valueOf(orderRequestDto.getQuantity()));
    if (user.getCashBalance().compareTo(totalCost) < 0) {
      throw new CustomException(TransactionErrorCode.INSUFFICIENT_BALANCE);
    }

    // 주문 ID 생성
    String orderId = userId + UUID.randomUUID().toString();

    // Redis 내 대기 주문 저장
    orderRedisService.saveOrder(
        market.getId().toString(),
        orderId,
        TradeType.BUY,
        orderRequestDto.getPrice(),
        orderRequestDto.getQuantity(),
        userId.toString());

    // 매칭 시도
    matchService.matchByMarketPrice(String.valueOf(marketId), marketPrice);

    return orderId;
  }

  /** 지정가 매도 */
  @Transactional
  public String limitSell(Long userId, Long marketId, OrderRequestDto orderRequestDto) {
    Market market =
        marketRepository
            .findById(marketId)
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> new CustomException(UserErrorCode.USER_NOT_FOUND, "해당 id의 유저가 존재하지 않습니다."));

    // TODO : 현재 시장가 불러오기
    // ✅ 현재 시장가 (실시간)
    BigDecimal marketPrice = priceQueryService.getLastTradePriceOrThrow(market.getSymbol());

    // 매도 가능한 수량을 가지고 있는지 확인
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

    // 주문 ID 생성
    String orderId = userId + UUID.randomUUID().toString();

    // Redis 내 대기 주문 저장
    orderRedisService.saveOrder(
        market.getId().toString(),
        orderId,
        TradeType.SELL,
        orderRequestDto.getPrice(),
        orderRequestDto.getQuantity(),
        userId.toString());

    // 매칭 시도
    matchService.matchByMarketPrice(String.valueOf(marketId), marketPrice);

    return orderId;
  }
}
