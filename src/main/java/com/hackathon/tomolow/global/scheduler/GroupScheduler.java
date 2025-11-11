package com.hackathon.tomolow.global.scheduler;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hackathon.tomolow.domain.group.entity.Group;
import com.hackathon.tomolow.domain.group.repository.GroupRepository;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.user.exception.UserErrorCode;
import com.hackathon.tomolow.domain.user.repository.UserRepository;
import com.hackathon.tomolow.domain.userGroup.dto.UserGroupRankingDto;
import com.hackathon.tomolow.domain.userGroup.entity.UserGroup;
import com.hackathon.tomolow.domain.userGroup.service.UserGroupRankingService;
import com.hackathon.tomolow.domain.userGroupTransaction.service.GroupOrderInfoService;
import com.hackathon.tomolow.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GroupScheduler {

  private final GroupRepository groupRepository;
  private final UserGroupRankingService userGroupRankingService;
  private final GroupOrderInfoService groupOrderInfoService;
  private final UserRepository userRepository;

  @Transactional
  @Scheduled(fixedRate = 300000) // 5분마다
  public void checkExpiredGroups() {
    LocalDateTime now = LocalDateTime.now();
    List<Group> expiredGroups = groupRepository.findExpiredGroups(now);
    for (Group group : expiredGroups) {
      Long winnerId = 0L;
      // 최종 pnl 저장
      List<UserGroupRankingDto> rankingAndPnLInGroup =
          userGroupRankingService.getRankingAndPnLInGroup(group.getId());
      for (UserGroupRankingDto rankingDto : rankingAndPnLInGroup) {
        UserGroup userGroup =
            groupOrderInfoService.getUserGroup(rankingDto.getUserPnl().getUserId(), group.getId());
        userGroup.setGroupFinalPnL(rankingDto.getUserPnl().getPnL());
        // 1위인 user의 id 저장
        if (rankingDto.getRanking() == 1) winnerId = rankingDto.getUserPnl().getUserId();
      }
      // 비활성화
      group.setGroupActive(false);
      // 1등에게 나머지 팀원의 시드머니 몰아주기
      User winner =
          userRepository
              .findById(winnerId)
              .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
      winner.addCashBalance(
          group.getSeedMoney().multiply(BigDecimal.valueOf(group.getMemberCount())));
    }
  }
}
