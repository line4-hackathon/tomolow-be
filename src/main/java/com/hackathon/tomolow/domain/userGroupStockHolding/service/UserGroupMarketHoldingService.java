package com.hackathon.tomolow.domain.userGroupStockHolding.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hackathon.tomolow.domain.ticker.service.PriceQueryService;
import com.hackathon.tomolow.domain.userGroup.entity.UserGroup;
import com.hackathon.tomolow.domain.userGroupStockHolding.dto.UserGroupMarketHoldingPnLDto;
import com.hackathon.tomolow.domain.userGroupStockHolding.entity.UserGroupMarketHolding;
import com.hackathon.tomolow.domain.userGroupStockHolding.repository.UserGroupMarketHoldingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserGroupMarketHoldingService {

  private final UserGroupMarketHoldingRepository userGroupMarketHoldingRepository;
  private final PriceQueryService priceQueryService;

  public UserGroupMarketHoldingPnLDto getPnLByUserGroupAndMarket(UserGroup userGroup) {

    // 1. 사용자가 해당 그룹 내에서 소유하고 있는 마켓 조회
    List<UserGroupMarketHolding> userGroupMarketHoldings =
        userGroupMarketHoldingRepository.findByUserGroup_Id(userGroup.getId()).orElse(null);

    if (userGroupMarketHoldings == null) return null;

    // 2. 마켓별로 손익금액과 손익률 계산
    List<UserGroupMarketHoldingPnLDto.SinglePnLDto> pnLDtos = new ArrayList<>();
    for (UserGroupMarketHolding holding : userGroupMarketHoldings) {
      // 2-1. 해당 마켓의 평균 매수가 / 실시간 가격 조회
      BigDecimal avgPrice = holding.getAvgPrice();
      BigDecimal lastTradePrice =
          priceQueryService.getLastTradePriceOrThrow(holding.getMarket().getSymbol());

      // 2-2. 손익금 계산
      BigDecimal pnL =
          (lastTradePrice.subtract(avgPrice)).multiply(BigDecimal.valueOf(holding.getQuantity()));
      // 2-3. 손익률 계산
      BigDecimal pnLRate =
          lastTradePrice
              .subtract(avgPrice)
              .divide(avgPrice, 6, RoundingMode.HALF_UP)
              .multiply(new BigDecimal(100))
              .setScale(1, RoundingMode.HALF_UP);

      UserGroupMarketHoldingPnLDto.SinglePnLDto singlePnLDto =
          UserGroupMarketHoldingPnLDto.SinglePnLDto.builder()
              .pnL(pnL)
              .pnLRate(pnLRate)
              .marketName(holding.getMarket().getName())
              .marketId(holding.getMarket().getId())
              .build();
      pnLDtos.add(singlePnLDto);
    }

    return UserGroupMarketHoldingPnLDto.builder().pnLDtos(pnLDtos).build();
  }
}
