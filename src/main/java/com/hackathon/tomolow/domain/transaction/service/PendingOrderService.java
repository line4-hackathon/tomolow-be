package com.hackathon.tomolow.domain.transaction.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.market.exception.MarketErrorCode;
import com.hackathon.tomolow.domain.market.repository.MarketRepository;
import com.hackathon.tomolow.domain.ticker.service.PriceQueryService;
import com.hackathon.tomolow.domain.transaction.dto.PendingOrderModifyRequestDto;
import com.hackathon.tomolow.domain.transaction.entity.TradeType;
import com.hackathon.tomolow.domain.transaction.exception.TransactionErrorCode;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.user.exception.UserErrorCode;
import com.hackathon.tomolow.domain.user.repository.UserRepository;
import com.hackathon.tomolow.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PendingOrderService {

  private final PriceQueryService priceQueryService;
  private final MarketRepository marketRepository;
  private final OrderRedisService orderRedisService;
  private final UserRepository userRepository;
  private final MatchService matchService;

  public BigDecimal getLatestMarketPrice(String orderId) {
    String marketId = orderRedisService.getOrderMarketId(orderId);
    if (marketId == null) throw new CustomException(TransactionErrorCode.PENDING_ORDER_NOT_EXIST);

    Market market =
        marketRepository
            .findById(Long.valueOf(marketId))
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));
    return priceQueryService.getLastTradePriceOrThrow(market.getSymbol());
  }

  public void modifyPendingOrder(Long userId, PendingOrderModifyRequestDto modifyRequestDto) {

    String orderId = modifyRequestDto.getOrderId();
    BigDecimal price = modifyRequestDto.getPrice();

    String marketId = orderRedisService.getOrderMarketId(orderId);
    if (marketId == null) throw new CustomException(TransactionErrorCode.PENDING_ORDER_NOT_EXIST);

    Market market =
        marketRepository
            .findById(Long.valueOf(marketId))
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    TradeType tradeType = orderRedisService.getTradeType(orderId);
    if (tradeType == null) throw new CustomException(TransactionErrorCode.TRADE_TYPE_NULL);

    // 매수 주문 - ( 갱신할 가격 * 수량 ) > user의 현금 자산인 경우 수정 불가
    if (tradeType == TradeType.BUY) {
      int remainingQuantity = orderRedisService.getRemainingQuantity(orderId);
      BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(remainingQuantity));
      if (totalPrice.compareTo(user.getCashBalance()) > 0)
        throw new CustomException(
            TransactionErrorCode.INSUFFICIENT_BALANCE, "잔액이 부족해서 수정할 수 없습니다.");
    }

    // order book (ZSET) 갱신
    orderRedisService.updateOrderBook(orderId, marketId, tradeType, price);

    // detail (HASH) 갱신
    orderRedisService.updatePrice(orderId, price);

    // 매칭 시도
    BigDecimal marketPrice = priceQueryService.getLastTradePriceOrThrow(market.getSymbol());
    matchService.matchByMarketPrice(marketId, marketPrice);
  }

  public void deletePendingOrder(Long userId, String orderId) {
    userRepository
        .findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    String marketId = orderRedisService.getOrderMarketId(orderId);
    if (marketId == null) throw new CustomException(TransactionErrorCode.PENDING_ORDER_NOT_EXIST);

    marketRepository
        .findById(Long.valueOf(marketId))
        .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));

    TradeType tradeType = orderRedisService.getTradeType(orderId);
    if (tradeType == null) throw new CustomException(TransactionErrorCode.TRADE_TYPE_NULL);

    orderRedisService.removeOrder(marketId, tradeType, orderId);
  }
}
