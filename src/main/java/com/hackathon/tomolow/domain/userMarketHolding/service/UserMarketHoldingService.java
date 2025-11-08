package com.hackathon.tomolow.domain.userMarketHolding.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.ticker.service.PriceQueryService;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.user.exception.UserErrorCode;
import com.hackathon.tomolow.domain.user.repository.UserRepository;
import com.hackathon.tomolow.domain.userInterestedMarket.repository.UserInterestedMarketRepository;
import com.hackathon.tomolow.domain.userMarketHolding.dto.UserMarketHoldingResponseDto;
import com.hackathon.tomolow.domain.userMarketHolding.entity.UserMarketHolding;
import com.hackathon.tomolow.domain.userMarketHolding.repository.UserMarketHoldingRepository;
import com.hackathon.tomolow.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserMarketHoldingService {

  private final UserRepository userRepository;
  private final UserMarketHoldingRepository userMarketHoldingRepository;
  private final PriceQueryService priceQueryService;
  private final UserInterestedMarketRepository userInterestedMarketRepository;

  public List<UserMarketHoldingResponseDto> getUserMarketHoldings(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    List<UserMarketHolding> allByUser = userMarketHoldingRepository.findAllByUser(user);

    List<UserMarketHoldingResponseDto> responseDtos = new ArrayList<>();

    for (UserMarketHolding holding : allByUser) {
      // 1. 마켓 정보 추출
      Market market = holding.getMarket();
      String symbol = market.getSymbol();

      // 2. 현재 가격 조회
      BigDecimal price =
          priceQueryService.getLastTradePriceOrThrow(symbol).setScale(0, RoundingMode.HALF_UP);

      // 3. 손익률 계산
      BigDecimal investAvgPrice = holding.getAvgPrice();

      BigDecimal pnlRate =
          price
              .subtract(investAvgPrice)
              .divide(investAvgPrice, 6, RoundingMode.HALF_UP)
              .multiply(new BigDecimal(100))
              .setScale(1, RoundingMode.HALF_UP);

      // 4. 관심 여부 조회
      boolean isInterested =
          userInterestedMarketRepository.existsByUser_IdAndMarket_Id(userId, market.getId());

      UserMarketHoldingResponseDto userMarketHoldingResponseDto =
          UserMarketHoldingResponseDto.builder()
              .symbol(symbol)
              .name(market.getName())
              .imageUrl(market.getImgUrl())
              .price(price)
              .changeRate(pnlRate)
              .interested(isInterested)
              .build();

      responseDtos.add(userMarketHoldingResponseDto);
    }

    return responseDtos;
  }
}
