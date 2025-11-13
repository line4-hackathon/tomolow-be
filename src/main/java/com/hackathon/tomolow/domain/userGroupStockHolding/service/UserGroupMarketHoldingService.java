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
import com.hackathon.tomolow.domain.userGroupTransaction.service.GroupOrderInfoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserGroupMarketHoldingService {

  private final UserGroupMarketHoldingRepository userGroupMarketHoldingRepository;
  private final PriceQueryService priceQueryService;
  private final GroupOrderInfoService groupOrderInfoService;

  /** 마켓별로 사용자의 PnL 계산 */
  public UserGroupMarketHoldingPnLDto getPnLByUserGroupAndMarket(UserGroup userGroup) {

    // 1. 사용자가 해당 그룹 내에서 소유하고 있는 마켓 조회
    List<UserGroupMarketHolding> userGroupMarketHoldings =
        userGroupMarketHoldingRepository.findByUserGroup_Id(userGroup.getId());

    if (userGroupMarketHoldings.isEmpty()) {
      return UserGroupMarketHoldingPnLDto.builder().pnLDtos(List.of()).build();
    }

    // 2. 마켓별로 손익금액과 손익률 계산
    List<UserGroupMarketHoldingPnLDto.SinglePnLDto> pnLDtos = new ArrayList<>();
    for (UserGroupMarketHolding holding : userGroupMarketHoldings) {
      // 2-1. 해당 마켓의 평균 매수가 / 실시간 가격 조회
      BigDecimal avgPrice = holding.getAvgPrice();
      BigDecimal lastTradePrice = null;

      try {
        lastTradePrice =
            priceQueryService.getLastTradePriceOrThrow(holding.getMarket().getSymbol());
      } catch (Exception e) {
        UserGroupMarketHoldingPnLDto.SinglePnLDto singlePnLDto =
            UserGroupMarketHoldingPnLDto.SinglePnLDto.builder()
                .pnL(BigDecimal.ZERO)
                .pnLRate(BigDecimal.ZERO)
                .marketName(holding.getMarket().getName())
                .marketId(holding.getMarket().getId())
                .marketImgUrl(holding.getMarket().getImgUrl())
                .totalPrice(BigDecimal.ZERO) // 마켓 총 보유 금액
                .quantity(holding.getQuantity())
                .build();
        pnLDtos.add(singlePnLDto);
        log.error(
            "실시간 시세 조회 실패 - " + holding.getMarket().getId() + " " + holding.getMarket().getName());
        continue;
      }

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

      // 2-4. 마켓별 손익금액/손익률 등을 담는 SinglePnLDto 생성
      UserGroupMarketHoldingPnLDto.SinglePnLDto singlePnLDto =
          UserGroupMarketHoldingPnLDto.SinglePnLDto.builder()
              .pnL(pnL)
              .pnLRate(pnLRate)
              .marketName(holding.getMarket().getName())
              .marketId(holding.getMarket().getId())
              .marketImgUrl(holding.getMarket().getImgUrl())
              .totalPrice(
                  lastTradePrice
                      .multiply(BigDecimal.valueOf(holding.getQuantity()))
                      .setScale(0, RoundingMode.DOWN)) // 마켓 총 보유 금액
              .quantity(holding.getQuantity())
              .build();
      pnLDtos.add(singlePnLDto);
    }

    return UserGroupMarketHoldingPnLDto.builder().pnLDtos(pnLDtos).build();
  }

  /** 보유 종목 조회 */
  public UserGroupMarketHoldingPnLDto getUserGroupMarketHoldings(Long userId, Long groupId) {
    // 해당 id의 사용자와 그룹이 존재하는지, 비활성화 여부 확인
    UserGroup userGroup = groupOrderInfoService.getUserGroup(userId, groupId);

    UserGroupMarketHoldingPnLDto pnLByUserGroupAndMarket = getPnLByUserGroupAndMarket(userGroup);

    return pnLByUserGroupAndMarket;
  }
}
