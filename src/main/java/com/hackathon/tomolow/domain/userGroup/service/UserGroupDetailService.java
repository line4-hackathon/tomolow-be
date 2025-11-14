package com.hackathon.tomolow.domain.userGroup.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.hackathon.tomolow.domain.group.entity.Group;
import com.hackathon.tomolow.domain.group.exception.GroupErrorCode;
import com.hackathon.tomolow.domain.group.repository.GroupRepository;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.user.exception.UserErrorCode;
import com.hackathon.tomolow.domain.user.repository.UserRepository;
import com.hackathon.tomolow.domain.userGroup.dto.UserGroupDetailResponseDto;
import com.hackathon.tomolow.domain.userGroup.dto.UserGroupRankingDto;
import com.hackathon.tomolow.domain.userGroup.entity.UserGroup;
import com.hackathon.tomolow.domain.userGroup.exception.UserGroupErrorCode;
import com.hackathon.tomolow.domain.userGroup.repository.UserGroupRepository;
import com.hackathon.tomolow.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserGroupDetailService {

  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final UserGroupRepository userGroupRepository;
  private final UserGroupRankingService userGroupRankingService;

  public UserGroupDetailResponseDto getUserGroupDetails(Long userId, Long groupId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> new CustomException(UserErrorCode.USER_NOT_FOUND, "해당 id의 유저가 존재하지 않습니다."));
    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new CustomException(GroupErrorCode.GROUP_NOT_FOUND));

    long currentMemberCount = userGroupRepository.countByGroup_Id(group.getId());
    BigDecimal seedMoney = group.getSeedMoney().setScale(0, RoundingMode.DOWN);

    // UserGroup 조회
    UserGroup userGroup =
        userGroupRepository
            .findByUser_IdAndGroup_Id(userId, groupId)
            .orElseThrow(
                () ->
                    new CustomException(UserGroupErrorCode.USER_GROUP_NOT_FOUND, "가입하지 않은 그룹입니다."));

    // 아직 모집 중인 그룹
    if (group.getActivatedAt() == null) {
      return UserGroupDetailResponseDto.builder()
          .groupName(group.getName())
          .groupId(group.getId())
          .code(group.getCode())
          .memberCount(group.getMemberCount())
          .currentMemberCount(currentMemberCount)
          .seedMoney(seedMoney)
          .build();
    }

    Duration duration = Duration.between(LocalDateTime.now(), group.getEndAt());
    long days = duration.toDays();
    long hours = duration.toHours() % 24;
    long minutes = duration.toMinutes() % 60;

    // 종료된 그룹
    if (group.getActivatedAt() != null && !group.getIsActive()) {
      days = 0;
      hours = 0;
      minutes = 0;
    }

    List<UserGroupRankingDto> rankingAndPnLInGroup =
        userGroupRankingService.getRankingAndPnLInGroup(groupId);
    Map<String, BigDecimal> pnLAndPnLRate =
        userGroupRankingService.getMyRankingAndPnLInGroup(userGroup);

    return UserGroupDetailResponseDto.builder()
        .groupName(group.getName())
        .groupId(group.getId())
        .code(group.getCode())
        .memberCount(group.getMemberCount())
        .currentMemberCount(currentMemberCount)
        .seedMoney(seedMoney)
        .days(days)
        .hours(hours)
        .minutes(minutes)
        .investmentBalance(userGroup.getInvestmentBalance().setScale(0, RoundingMode.DOWN))
        .cashBalance(userGroup.getCashBalance().setScale(0, RoundingMode.DOWN))
        .totalBalance(
            userGroup
                .getCashBalance()
                .add(userGroup.getInvestmentBalance())
                .setScale(0, RoundingMode.DOWN))
        .pnL(pnLAndPnLRate.get("pnL"))
        .pnLRate(pnLAndPnLRate.get("pnLRate"))
        .rankings(rankingAndPnLInGroup)
        .build();
  }
}
