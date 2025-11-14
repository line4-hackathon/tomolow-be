package com.hackathon.tomolow.domain.userGroup.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.hackathon.tomolow.domain.group.entity.Group;
import com.hackathon.tomolow.domain.group.exception.GroupErrorCode;
import com.hackathon.tomolow.domain.group.repository.GroupRepository;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.userGroup.dto.UserGroupRankingDto;
import com.hackathon.tomolow.domain.userGroup.entity.UserGroup;
import com.hackathon.tomolow.domain.userGroup.exception.UserGroupErrorCode;
import com.hackathon.tomolow.domain.userGroup.repository.UserGroupRepository;
import com.hackathon.tomolow.domain.userGroupStockHolding.dto.UserGroupMarketHoldingPnLDto;
import com.hackathon.tomolow.domain.userGroupStockHolding.service.UserGroupMarketHoldingService;
import com.hackathon.tomolow.domain.userGroupTransaction.entity.UserGroupTransaction;
import com.hackathon.tomolow.domain.userGroupTransaction.repository.UserGroupTransactionRepository;
import com.hackathon.tomolow.domain.userGroupTransaction.service.GroupOrderInfoService;
import com.hackathon.tomolow.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserGroupRankingService {

  private final UserGroupRepository userGroupRepository;
  private final GroupRepository groupRepository;
  private final UserGroupMarketHoldingService userGroupMarketHoldingService;
  private final GroupOrderInfoService groupOrderInfoService;
  private final UserGroupTransactionRepository userGroupTransactionRepository;

  /** 그룹 내 랭킹과 손익금액 조회 */
  public List<UserGroupRankingDto> getRankingAndPnLInGroup(Long groupId) {
    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new CustomException(GroupErrorCode.GROUP_NOT_FOUND));

    // 1. 종료된 그룹은 DB에서 가져오기
    if (!group.getIsActive() && group.getActivatedAt() != null) {
      return getRankingAndPnLOfExpiredGroup(groupId);
    }

    // 2. 모집 중인 그룹은 손익 계산 불가
    if (group.getActivatedAt() == null) {
      throw new CustomException(
          UserGroupErrorCode.GROUP_NOT_ACTIVE_YET, "그룹이 아직 모집 중 상태라 랭킹을 계산할 수 없습니다.");
    }

    // 3. 그룹에 가입 중인 사용자 가져오기
    List<UserGroup> userGroups =
        userGroupRepository
            .findByGroup_Id(groupId)
            .orElseThrow(() -> new CustomException(UserGroupErrorCode.USER_GROUP_NOT_FOUND));

    // 4. 그룹의 모든 사용자의 손익 계산
    List<UserGroupRankingDto.UserPnLDto> userPnLDtos = new ArrayList<>();
    for (UserGroup userGroup : userGroups) {
      // 4-1. 사용자별 마켓별 손익 조회
      UserGroupMarketHoldingPnLDto pnLByUserGroupAndMarket =
          userGroupMarketHoldingService.getPnLByUserGroupAndMarket(userGroup);
      List<UserGroupMarketHoldingPnLDto.SinglePnLDto> pnLDtos =
          pnLByUserGroupAndMarket.getPnLDtos();

      // 4-2. 전체 손익금액 계산 = (남아있는 현금 자산 + 현재 투자 자산) - 시드머니
      BigDecimal totalPrice = BigDecimal.ZERO;
      for (UserGroupMarketHoldingPnLDto.SinglePnLDto pnLDto : pnLDtos) {
        totalPrice = totalPrice.add(pnLDto.getTotalPrice());
      }
      BigDecimal pnL =
          userGroup
              .getCashBalance()
              .add(totalPrice)
              .subtract(userGroup.getGroup().getSeedMoney())
              .setScale(0, RoundingMode.DOWN);

      // 4-3. UserPnLDto 생성
      User user = userGroup.getUser();
      UserGroupRankingDto.UserPnLDto userPnLDto =
          UserGroupRankingDto.UserPnLDto.builder()
              .userId(user.getId())
              .nickName(user.getNickname())
              .pnL(pnL)
              .build();

      // 4-4. UserGroupRankingDto에 추가
      userPnLDtos.add(userPnLDto);
    }

    // 5. userPnlDtos를 PnL 내림차순으로 정렬
    userPnLDtos.sort((a, b) -> b.getPnL().compareTo(a.getPnL()));

    // 6. PnL 높은 순으로 랭킹과 함께 반환
    List<UserGroupRankingDto> userGroupRankingDtos = new ArrayList<>();
    int rank = 1;
    for (UserGroupRankingDto.UserPnLDto pnLDto : userPnLDtos) {
      userGroupRankingDtos.add(UserGroupRankingDto.builder().userPnl(pnLDto).ranking(rank).build());
      rank++;
    }

    return userGroupRankingDtos;
  }

  /** 종료된 그룹의 랭킹과 손익 가져오기 */
  public List<UserGroupRankingDto> getRankingAndPnLOfExpiredGroup(Long groupId) {
    List<UserGroup> userGroups =
        userGroupRepository
            .findByGroup_Id(groupId)
            .orElseThrow(() -> new CustomException(UserGroupErrorCode.USER_GROUP_NOT_FOUND));
    // 1. 그룹 내 가입한 사용자들의 PnL 불러오기
    List<UserGroupRankingDto.UserPnLDto> userPnLDtoList = new ArrayList<>();
    for (UserGroup userGroup : userGroups) {
      BigDecimal finalPnl = userGroup.getFinalPnl().setScale(0, RoundingMode.DOWN);
      User user = userGroup.getUser();
      UserGroupRankingDto.UserPnLDto userPnLDto =
          UserGroupRankingDto.UserPnLDto.builder()
              .nickName(user.getNickname())
              .pnL(finalPnl)
              .userId(user.getId())
              .build();
      userPnLDtoList.add(userPnLDto);
    }
    // 2. 정렬
    userPnLDtoList.sort((a, b) -> b.getPnL().compareTo(a.getPnL()));
    // 3. PnL 높은 순으로 랭킹과 함께 반환
    List<UserGroupRankingDto> userGroupRankingDtos = new ArrayList<>();
    int rank = 1;
    for (UserGroupRankingDto.UserPnLDto pnLDto : userPnLDtoList) {
      userGroupRankingDtos.add(UserGroupRankingDto.builder().userPnl(pnLDto).ranking(rank).build());
      rank++;
    }
    return userGroupRankingDtos;
  }

  /** 그룹 내 내 순위만 조회 */
  public int getMyRanking(Long userId, Long groupId) {
    List<UserGroupRankingDto> rankingAndPnLInGroup = getRankingAndPnLInGroup(groupId);
    for (UserGroupRankingDto userRanking : rankingAndPnLInGroup) {
      if (userRanking.getUserPnl().getUserId().equals(userId)) {
        return userRanking.getRanking();
      }
    }
    return 0;
  }

  public Map<String, BigDecimal> getMyRankingAndPnLInGroup(UserGroup userGroup) {
    List<UserGroupRankingDto> rankingAndPnLInGroup =
        getRankingAndPnLInGroup(userGroup.getGroup().getId());
    Map<String, BigDecimal> result = new HashMap<>();
    BigDecimal pnL = null;
    BigDecimal pnLRate = BigDecimal.ZERO;

    // PnL 조회
    for (UserGroupRankingDto userRanking : rankingAndPnLInGroup) {
      if (userRanking.getUserPnl().getUserId().equals(userGroup.getUser().getId())) {
        pnL = userRanking.getUserPnl().getPnL().setScale(0, RoundingMode.DOWN);
      }
    }

    if (pnL == null) {
      result.put("pnL", BigDecimal.ZERO);
      result.put("pnLRate", BigDecimal.ZERO);
      return result;
    }

    // 2. 유저의 전체 매수매도 트랜잭션 불러오기
    List<UserGroupTransaction> allByUserGroupId =
        userGroupTransactionRepository.findAllByUserGroup_Id(userGroup.getId());

    BigDecimal totalBuy = BigDecimal.ZERO;
    BigDecimal totalSell = BigDecimal.ZERO;

    for (UserGroupTransaction tx : allByUserGroupId) {
      BigDecimal amount = tx.getPrice().multiply(BigDecimal.valueOf(tx.getQuantity()));

      if (tx.isBuy()) totalBuy = totalBuy.add(amount);
      else totalSell = totalSell.add(amount);
    }

    if (totalBuy.compareTo(BigDecimal.ZERO) == 0) {
      pnLRate = BigDecimal.ZERO;
    } else {
      pnLRate = pnL.divide(totalBuy, 6, RoundingMode.HALF_UP);
    }
    result.put("pnL", pnL);
    result.put("pnLRate", pnLRate);
    return result;
  }
}
