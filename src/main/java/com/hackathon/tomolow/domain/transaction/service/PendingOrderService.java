package com.hackathon.tomolow.domain.transaction.service;

import java.math.BigDecimal;

import com.hackathon.tomolow.domain.transaction.dto.PendingOrderModifyRequestDto;
import com.hackathon.tomolow.domain.transaction.entity.TradeType;
import com.hackathon.tomolow.domain.user.exception.UserErrorCode;
import com.hackathon.tomolow.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.market.exception.MarketErrorCode;
import com.hackathon.tomolow.domain.market.repository.MarketRepository;
import com.hackathon.tomolow.domain.ticker.service.PriceQueryService;
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

  public BigDecimal getLatestMarketPrice(Long marketId) {
    Market market =
        marketRepository
            .findById(marketId)
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));
    return priceQueryService.getLastTradePriceOrThrow(market.getSymbol());
  }

  public void modifyPendingOrder(Long userId, Long marketId, PendingOrderModifyRequestDto modifyRequestDto) {
    Market market = marketRepository
            .findById(marketId)
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));

    userRepository.findById(userId).orElseThrow(
            () -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    String orderId = modifyRequestDto.getOrderId();
    BigDecimal price = modifyRequestDto.getPrice();
    TradeType tradeType = modifyRequestDto.getTradeType();

    // order book (ZSET) 갱신
    orderRedisService.updateOrderBook(orderId, String.valueOf(marketId), tradeType, price);

    // detail (HASH) 갱신
    orderRedisService.updatePrice(modifyRequestDto.getOrderId(), modifyRequestDto.getPrice());

    // 매칭 시도
    BigDecimal marketPrice = priceQueryService.getLastTradePriceOrThrow(market.getSymbol());
    matchService.matchByMarketPrice(String.valueOf(marketId), marketPrice);
  }
}
