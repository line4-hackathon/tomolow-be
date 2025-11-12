package com.hackathon.tomolow.global.scheduler;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hackathon.tomolow.domain.group.entity.Group;
import com.hackathon.tomolow.domain.group.repository.GroupRepository;
import com.hackathon.tomolow.domain.user.repository.UserRepository;
import com.hackathon.tomolow.domain.userGroup.dto.UserGroupRankingDto;
import com.hackathon.tomolow.domain.userGroup.entity.UserGroup;
import com.hackathon.tomolow.domain.userGroup.service.UserGroupRankingService;
import com.hackathon.tomolow.domain.userGroupTransaction.repository.UserGroupTransactionRepository;
import com.hackathon.tomolow.domain.userGroupTransaction.service.GroupOrderInfoService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GroupScheduler {

  private final GroupRepository groupRepository;
  private final UserGroupRankingService userGroupRankingService;
  private final GroupOrderInfoService groupOrderInfoService;
  private final UserRepository userRepository;
  private final UserGroupTransactionRepository userGroupTransactionRepository;

  @Transactional
  @Scheduled(fixedRate = 300000) // 5분마다
  public void checkExpiredGroups() {
    LocalDateTime now = LocalDateTime.now();
    List<Group> expiredGroups = groupRepository.findExpiredGroups(now);
    for (Group group : expiredGroups) {
      Long winnerId = 0L;
      // 1. 최종 pnl 저장
      List<UserGroupRankingDto> rankingAndPnLInGroup =
          userGroupRankingService.getRankingAndPnLInGroup(group.getId());
      for (UserGroupRankingDto rankingDto : rankingAndPnLInGroup) {
        UserGroup userGroup =
            groupOrderInfoService.getUserGroup(rankingDto.getUserPnl().getUserId(), group.getId());
        userGroup.setGroupFinalPnL(rankingDto.getUserPnl().getPnL());
        // 1위인 user의 id 저장
        if (rankingDto.getRanking() == 1) winnerId = rankingDto.getUserPnl().getUserId();
      }

      // 2. 비활성화
      group.setGroupActive(false);

      // 3. 1등에게 나머지 팀원의 시드머니 몰아주기
      // 3-1. 그룹 내 거래가 없었던 경우 몰아주기 X 다시 분배
      if (!userGroupTransactionRepository.existsByUserGroup_Group_Id(group.getId())) {
        for (UserGroupRankingDto rankingDto : rankingAndPnLInGroup) {
          Long userId = rankingDto.getUserPnl().getUserId();
          userRepository
              .findById(userId)
              .ifPresent(
                  (user) -> {
                    user.addCashBalance(group.getSeedMoney());
                  });
        }
        return;
      }
      // 3-2. 시드머니 몰아주기
      userRepository
          .findById(winnerId)
          .ifPresent(
              (winner) -> {
                winner.addCashBalance(
                    group.getSeedMoney().multiply(BigDecimal.valueOf(group.getMemberCount())));
              });
    }
  }
}
