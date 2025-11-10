package com.hackathon.tomolow.domain.group.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hackathon.tomolow.domain.group.dto.GroupListResponseDto;
import com.hackathon.tomolow.domain.group.dto.JoinableGroupListResponseDto;
import com.hackathon.tomolow.domain.group.entity.Group;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.user.exception.UserErrorCode;
import com.hackathon.tomolow.domain.user.repository.UserRepository;
import com.hackathon.tomolow.domain.userGroup.entity.UserGroup;
import com.hackathon.tomolow.domain.userGroup.repository.UserGroupRepository;
import com.hackathon.tomolow.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupListService {

  private final UserRepository userRepository;
  private final UserGroupRepository userGroupRepository;

  public GroupListResponseDto getActiveAndExpiredGroupList(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    // 1. 현재 active이고 userId에 해당하는 그룹 조회
    List<Group> activeGroups =
        userGroupRepository.findByUser_IdAndGroup_IsActive(userId, true).stream()
            .map(UserGroup::getGroup)
            .toList();

    List<GroupListResponseDto.GroupSummary> activeGroupLists = new ArrayList<>();
    for (Group group : activeGroups) {
      // 종료까지 남은 시간 계산
      Duration timeUntilEnd = Duration.between(LocalDateTime.now(), group.getEndAt());
      // TODO : 랭킹 구현 시 업데이트 필요
      Long dayUntilEnd = timeUntilEnd.toDays();
      Long hourUntilEnd = timeUntilEnd.toHours() % 24;

      GroupListResponseDto.GroupSummary groupSummary =
          GroupListResponseDto.GroupSummary.builder()
              .groupName(group.getName())
              .ranking(1)
              .dayUntilEnd(dayUntilEnd)
              .hourUntilEnd(hourUntilEnd)
              .groupId(group.getId())
              .build();

      activeGroupLists.add(groupSummary);
    }

    // 2. userId에 해당하고 종료된 그룹 조회
    List<Group> expiredGroups =
        userGroupRepository.findExpiredGroupsByUser(userId, LocalDateTime.now()).stream()
            .map(UserGroup::getGroup)
            .toList();

    List<GroupListResponseDto.GroupSummary> expiredGroupList = new ArrayList<>();
    for (Group group : expiredGroups) {
      // TODO : 랭킹 구현 시 업데이트 필요
      GroupListResponseDto.GroupSummary groupSummary =
          GroupListResponseDto.GroupSummary.builder()
              .groupName(group.getName())
              .ranking(1)
              .dayUntilEnd(null)
              .hourUntilEnd(null)
              .groupId(group.getId())
              .build();

      expiredGroupList.add(groupSummary);
    }

    return GroupListResponseDto.builder()
        .activeList(activeGroupLists)
        .expiredList(expiredGroupList)
        .build();
  }

  public JoinableGroupListResponseDto getJoinableGroupList(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    // activatedAt이 null이면서 user가 속한 그룹 조회 (아직 활성화 전)
    List<Group> groupsJoined =
        userGroupRepository.findJoinableGroupsByUser(userId).stream()
            .map(UserGroup::getGroup)
            .toList();

    List<JoinableGroupListResponseDto.JoinableGroupSummary> joinedGroupList =
        getJoinableGroupSummaries(groupsJoined);

    // activatedAt이 null이면서 user가 속하지 않은 그룹 조회 (아직 활성화 전)
    List<Group> groupsNotJoined = userGroupRepository.findJoinableGroupsExceptMine(userId);

    List<JoinableGroupListResponseDto.JoinableGroupSummary> notJoinedGroupList =
        getJoinableGroupSummaries(groupsNotJoined);

    return JoinableGroupListResponseDto.builder()
        .joinedGroupList(joinedGroupList)
        .notJoinedGroupList(notJoinedGroupList)
        .build();
  }

  public List<JoinableGroupListResponseDto.JoinableGroupSummary> getJoinableGroupSummaries(
      List<Group> groups) {
    List<JoinableGroupListResponseDto.JoinableGroupSummary> groupSummaryList = new ArrayList<>();
    for (Group group : groups) {
      long currentMemberCount = userGroupRepository.countByGroup_Id(group.getId());
      JoinableGroupListResponseDto.JoinableGroupSummary groupSummary =
          JoinableGroupListResponseDto.JoinableGroupSummary.builder()
              .groupName(group.getName())
              .memberCount(group.getMemberCount())
              .currentMemberCount((int) currentMemberCount)
              .groupId(group.getId())
              .build();
      groupSummaryList.add(groupSummary);
    }
    return groupSummaryList;
  }
}
