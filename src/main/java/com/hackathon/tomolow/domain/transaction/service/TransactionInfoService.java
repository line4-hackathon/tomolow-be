package com.hackathon.tomolow.domain.transaction.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.market.exception.MarketErrorCode;
import com.hackathon.tomolow.domain.market.repository.MarketRepository;
import com.hackathon.tomolow.domain.ticker.service.PriceQueryService;
import com.hackathon.tomolow.domain.transaction.dto.InfoResponseDto;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.user.exception.UserErrorCode;
import com.hackathon.tomolow.domain.user.repository.UserRepository;
import com.hackathon.tomolow.domain.userMarketHolding.entity.UserMarketHolding;
import com.hackathon.tomolow.domain.userMarketHolding.repository.UserMarketHoldingRepository;
import com.hackathon.tomolow.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionInfoService {

  private final PriceQueryService priceQueryService;
  private final MarketRepository marketRepository;
  private final UserRepository userRepository;
  private final UserMarketHoldingRepository userMarketHoldingRepository;

  /** 시장가 매수 -> 시장가 및 최대 매수 가능 수량 반환 */
  public InfoResponseDto getMarketBuyInfo(Long userId, Long marketId) {
    Market market =
        marketRepository
            .findById(marketId)
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    // 가장 최신 시장가 조회
    BigDecimal marketPrice = priceQueryService.getLastTradePriceOrThrow(market.getSymbol());
    BigDecimal cashBalance = user.getCashBalance();
    Long maxQuantity = cashBalance.divideToIntegralValue(marketPrice).longValue();

    return InfoResponseDto.builder()
        .marketPrice(marketPrice)
        .maxQuantity(maxQuantity)
        .userCashBalance(cashBalance.longValue())
        .build();
  }

  /** 지정가 매수 -> 최대 매수 가능 수량 반환 */
  public InfoResponseDto getLimitBuyInfo(Long userId, Long marketId, BigDecimal price) {
    Market market =
        marketRepository
            .findById(marketId)
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    BigDecimal cashBalance = user.getCashBalance();
    Long maxQuantity = cashBalance.divideToIntegralValue(price).longValue();

    return InfoResponseDto.builder()
        .marketPrice(null)
        .maxQuantity(maxQuantity)
        .userCashBalance(cashBalance.longValue())
        .build();
  }

  /** 시장가 / 지정가 매도 -> 시장가 및 최대 매도 가능 수량 반환 (공통) */
  public InfoResponseDto getSellInfo(Long userId, Long marketId) {
    Market market =
        marketRepository
            .findById(marketId)
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    BigDecimal marketPrice = priceQueryService.getLastTradePriceOrThrow(market.getSymbol());

    UserMarketHolding userMarketHolding =
        userMarketHoldingRepository.findByUserAndMarket(user, market).orElse(null);
    Long maxQuantity;
    if (userMarketHolding != null) {
      maxQuantity = userMarketHolding.getQuantity();
    } else {
      maxQuantity = 0L;
    }

    return InfoResponseDto.builder()
        .marketPrice(marketPrice)
        .maxQuantity(maxQuantity)
        .userCashBalance(null)
        .build();
  }
}
