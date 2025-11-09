package com.hackathon.tomolow.global.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hackathon.tomolow.domain.group.entity.Group;
import com.hackathon.tomolow.domain.group.repository.GroupRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GroupScheduler {

  private final GroupRepository groupRepository;

  @Scheduled(fixedRate = 300000) // 5분마다
  public void checkExpiredGroups() {
    LocalDateTime now = LocalDateTime.now();
    List<Group> expiredGroups = groupRepository.findExpiredGroups(now);
    for (Group group : expiredGroups) {
      // 비활성화
      group.setGroupActive(false);
      // TODO : 1등에게 나머지 팀원의 시드머니 몰아주기
    }
  }
}
