package com.hackathon.tomolow.domain.userGroup.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
import com.hackathon.tomolow.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserGroupRankingService {

  private final UserGroupRepository userGroupRepository;
  private final GroupRepository groupRepository;
  private final UserGroupMarketHoldingService userGroupMarketHoldingService;

  /** 그룹 내 랭킹과 손익금액 조회 */
  public List<UserGroupRankingDto> getRankingAndPnLInGroup(Long groupId) {
    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new CustomException(GroupErrorCode.GROUP_NOT_FOUND));

    // 1. 모집 중인 그룹은 손익 계산 불가
    if (group.getActivatedAt() == null) {
      throw new CustomException(
          UserGroupErrorCode.GROUP_NOT_ACTIVE_YET, "그룹이 아직 모집 중 상태라 랭킹을 계산할 수 없습니다.");
    }

    // 2. 그룹에 가입 중인 사용자 가져오기
    List<UserGroup> userGroups =
        userGroupRepository
            .findByGroup_Id(groupId)
            .orElseThrow(() -> new CustomException(UserGroupErrorCode.USER_GROUP_NOT_FOUND));

    // 3. 그룹의 모든 사용자의 손익 계산
    List<UserGroupRankingDto.UserPnLDto> userPnLDtos = new ArrayList<>();
    for (UserGroup userGroup : userGroups) {
      // 3-1. 사용자별 마켓별 손익 조회
      UserGroupMarketHoldingPnLDto pnLByUserGroupAndMarket =
          userGroupMarketHoldingService.getPnLByUserGroupAndMarket(userGroup);
      List<UserGroupMarketHoldingPnLDto.SinglePnLDto> pnLDtos =
          pnLByUserGroupAndMarket.getPnLDtos();

      // 3-2. 전체 손익금액 계산 = (남아있는 현금 자산 + 현재 투자 자산) - 시드머니
      BigDecimal totalPrice = BigDecimal.ZERO;
      for (UserGroupMarketHoldingPnLDto.SinglePnLDto pnLDto : pnLDtos) {
        totalPrice = totalPrice.add(pnLDto.getPnL());
      }
      BigDecimal pnL =
          userGroup.getCashBalance().add(totalPrice).subtract(userGroup.getGroup().getSeedMoney());

      // 3-3. UserPnLDto 생성
      User user = userGroup.getUser();
      UserGroupRankingDto.UserPnLDto userPnLDto =
          UserGroupRankingDto.UserPnLDto.builder()
              .userId(user.getId())
              .nickName(user.getNickname())
              .pnL(pnL)
              .build();

      // 3-4. UserGroupRankingDto에 추가
      userPnLDtos.add(userPnLDto);
    }

    // 4. userPnlDtos를 PnL 내림차순으로 정렬
    userPnLDtos.sort((a, b) -> b.getPnL().compareTo(a.getPnL()));

    // 5. PnL 높은 순으로 랭킹과 함께 반환
    List<UserGroupRankingDto> userGroupRankingDtos = new ArrayList<>();
    int rank = 1;
    for (UserGroupRankingDto.UserPnLDto pnLDto : userPnLDtos) {
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
}
