package com.hackathon.tomolow.domain.userGroupTransaction.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import com.hackathon.tomolow.domain.group.entity.Group;
import com.hackathon.tomolow.domain.group.exception.GroupErrorCode;
import com.hackathon.tomolow.domain.group.repository.GroupRepository;
import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.market.exception.MarketErrorCode;
import com.hackathon.tomolow.domain.market.repository.MarketRepository;
import com.hackathon.tomolow.domain.ticker.service.PriceQueryService;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.user.exception.UserErrorCode;
import com.hackathon.tomolow.domain.user.repository.UserRepository;
import com.hackathon.tomolow.domain.userGroup.entity.UserGroup;
import com.hackathon.tomolow.domain.userGroup.exception.UserGroupErrorCode;
import com.hackathon.tomolow.domain.userGroup.repository.UserGroupRepository;
import com.hackathon.tomolow.domain.userGroupStockHolding.entity.UserGroupMarketHolding;
import com.hackathon.tomolow.domain.userGroupStockHolding.repository.UserGroupMarketHoldingRepository;
import com.hackathon.tomolow.domain.userGroupTransaction.dto.GroupInfoResponseDto;
import com.hackathon.tomolow.domain.userGroupTransaction.exception.UserGroupTransactionErrorCode;
import com.hackathon.tomolow.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupOrderInfoService {

  private final MarketRepository marketRepository;
  private final GroupRepository groupRepository;
  private final UserRepository userRepository;
  private final PriceQueryService priceQueryService;
  private final UserGroupRepository userGroupRepository;
  private final UserGroupMarketHoldingRepository userGroupMarketHoldingRepository;

  /** 시장가 매수 -> 시장가 및 최대 매수 가능 수량 반환 */
  public GroupInfoResponseDto getGroupMarketBuyInfo(Long userId, Long groupId, Long marketId) {
    Market market =
        marketRepository
            .findById(marketId)
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));
    UserGroup userGroup = getUserGroup(userId, groupId);

    // 가장 최신 시장가 조회
    BigDecimal marketPrice =
        priceQueryService
            .getLastTradePriceOrThrow(market.getSymbol())
            .setScale(0, RoundingMode.DOWN);

    // 최대 매수 가능 수량
    BigDecimal cashBalance = userGroup.getCashBalance();
    Long maxQuantity = cashBalance.divideToIntegralValue(marketPrice).longValue();

    return GroupInfoResponseDto.builder()
        .marketPrice(marketPrice)
        .maxQuantity(maxQuantity)
        .userGroupCashBalance(cashBalance.longValue())
        .build();
  }

  /** 지정가 매수 -> 최대 매수 가능 수량 반환 */
  public GroupInfoResponseDto getGroupLimitBuyInfo(
      Long userId, Long groupId, Long marketId, BigDecimal price) {
    Market market =
        marketRepository
            .findById(marketId)
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));
    UserGroup userGroup = getUserGroup(userId, groupId);

    BigDecimal cashBalance = userGroup.getCashBalance();
    Long maxQuantity = cashBalance.divideToIntegralValue(price).longValue();

    return GroupInfoResponseDto.builder()
        .marketPrice(null)
        .maxQuantity(maxQuantity)
        .userGroupCashBalance(cashBalance.longValue())
        .build();
  }

  /** 시장가 / 지정가 매도 -> 시장가 및 최대 매도 가능 수량 반환 (공통) */
  public GroupInfoResponseDto getGroupSellInfo(Long userId, Long groupId, Long marketId) {
    Market market =
        marketRepository
            .findById(marketId)
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));
    UserGroup userGroup = getUserGroup(userId, groupId);

    BigDecimal marketPrice =
        priceQueryService
            .getLastTradePriceOrThrow(market.getSymbol())
            .setScale(0, RoundingMode.DOWN);

    UserGroupMarketHolding userGroupMarketHolding =
        userGroupMarketHoldingRepository
            .findByUserGroup_IdAndMarket_Id(userGroup.getId(), marketId)
            .orElse(null);
    Long maxQuantity;

    if (userGroupMarketHolding != null) {
      maxQuantity = userGroupMarketHolding.getQuantity();
    } else {
      maxQuantity = 0L;
    }

    return GroupInfoResponseDto.builder()
        .marketPrice(marketPrice)
        .maxQuantity(maxQuantity)
        .userGroupCashBalance(null)
        .build();
  }

  /** id에 해당하는 그룹, 유저 있는지 체크 -> UserGroup 반환 */
  public UserGroup getUserGroup(Long userId, Long groupId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> new CustomException(UserErrorCode.USER_NOT_FOUND, "해당 id의 유저가 존재하지 않습니다."));
    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new CustomException(GroupErrorCode.GROUP_NOT_FOUND));

    // 그룹 활성화 여부 조회
    if (group.getIsActive() == false)
      throw new CustomException(UserGroupTransactionErrorCode.GROUP_INACTIVE);

    // UserGroup 조회
    UserGroup userGroup =
        userGroupRepository
            .findByUser_IdAndGroup_Id(userId, groupId)
            .orElseThrow(
                () ->
                    new CustomException(UserGroupErrorCode.USER_GROUP_NOT_FOUND, "가입하지 않은 그룹입니다."));
    return userGroup;
  }
}
