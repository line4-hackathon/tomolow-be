package com.hackathon.tomolow.domain.userGroupTransaction.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.market.exception.MarketErrorCode;
import com.hackathon.tomolow.domain.market.repository.MarketRepository;
import com.hackathon.tomolow.domain.transaction.dto.OrderRequestDto;
import com.hackathon.tomolow.domain.transaction.entity.TradeType;
import com.hackathon.tomolow.domain.userGroup.entity.UserGroup;
import com.hackathon.tomolow.domain.userGroupStockHolding.entity.UserGroupMarketHolding;
import com.hackathon.tomolow.domain.userGroupStockHolding.exception.UserGroupMarketHoldingErrorCode;
import com.hackathon.tomolow.domain.userGroupStockHolding.repository.UserGroupMarketHoldingRepository;
import com.hackathon.tomolow.domain.userGroupTransaction.entity.UserGroupTransaction;
import com.hackathon.tomolow.domain.userGroupTransaction.exception.UserGroupTransactionErrorCode;
import com.hackathon.tomolow.domain.userGroupTransaction.repository.UserGroupTransactionRepository;
import com.hackathon.tomolow.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketGroupOrderService {

  private final MarketRepository marketRepository;
  private final UserGroupMarketHoldingRepository userGroupMarketHoldingRepository;
  private final UserGroupTransactionRepository userGroupTransactionRepository;
  private final GroupOrderInfoService groupOrderInfoService;

  /** 시장가 자체를 orderRequestDto로 전달받기 때문에 시장가 조회 필요 X */
  /** 시장가 매수 */
  @Transactional
  public void marketBuy(Long userId, Long groupId, Long marketId, OrderRequestDto orderRequestDto) {
    Market market =
        marketRepository
            .findById(marketId)
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));

    BigDecimal price = orderRequestDto.getPrice();
    int quantity = orderRequestDto.getQuantity();

    // 1. UserGroup 조회
    UserGroup userGroup = groupOrderInfoService.getUserGroup(userId, groupId);

    // 2. 매수 가능 잔고인지 확인
    BigDecimal totalCost = price.multiply(BigDecimal.valueOf(quantity));
    if (userGroup.getCashBalance().compareTo(totalCost) < 0) {
      throw new CustomException(UserGroupTransactionErrorCode.INSUFFICIENT_BALANCE);
    }

    // 3. 사용자의 그룹 내 자산 변화
    userGroup.subtractCash(totalCost);
    userGroup.addInvestment(totalCost);

    // 4. 보유 수량 갱신
    UserGroupMarketHolding userGroupMarketHolding =
        userGroupMarketHoldingRepository
            .findByUserGroup_IdAndMarket_Id(userGroup.getId(), marketId)
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
    userGroupMarketHolding.addQuantity(quantity, price);

    // 5. 거래 내역 DB에 저장
    UserGroupTransaction transaction =
        UserGroupTransaction.builder()
            .tradeType(TradeType.BUY)
            .quantity(quantity)
            .price(price)
            .userGroup(userGroup)
            .market(market)
            .build();
    userGroupTransactionRepository.save(transaction);

    log.info("그룹 시장가 매수");
  }

  /** 시장가 매도 */
  @Transactional
  public void marketSell(
      Long userId, Long groupId, Long marketId, OrderRequestDto orderRequestDto) {
    Market market =
        marketRepository
            .findById(marketId)
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));

    BigDecimal price = orderRequestDto.getPrice();
    int quantity = orderRequestDto.getQuantity();

    // 1. UserGroup 조회
    UserGroup userGroup = groupOrderInfoService.getUserGroup(userId, groupId);

    // 2. 매도 가능한 수량을 가지고 있는지 확인
    UserGroupMarketHolding userGroupMarketHolding =
        userGroupMarketHoldingRepository
            .findByUserGroup_IdAndMarket_Id(userGroup.getId(), marketId)
            .orElseThrow(
                () ->
                    new CustomException(
                        UserGroupMarketHoldingErrorCode.HOLDING_NOT_FOUND,
                        "유저가 해당 종목을 보유하고 있지 않습니다."));
    if (userGroupMarketHolding.getQuantity() < orderRequestDto.getQuantity()) {
      throw new CustomException(UserGroupMarketHoldingErrorCode.INSUFFICIENT_QUANTITY);
    }

    // 3. 보유 수량 감소
    userGroupMarketHolding.subtractQuantity(quantity);

    // 4. 사용자 자산 변화
    BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(quantity));
    userGroup.addCash(totalPrice);
    userGroup.subtractInvestment(totalPrice);

    // 5. 체결 내역 DB에 저장
    UserGroupTransaction transaction =
        UserGroupTransaction.builder()
            .market(market)
            .userGroup(userGroup)
            .price(price)
            .quantity(quantity)
            .tradeType(TradeType.SELL)
            .build();
    userGroupTransactionRepository.save(transaction);

    log.info("그룹 시장가 매도");
  }
}
