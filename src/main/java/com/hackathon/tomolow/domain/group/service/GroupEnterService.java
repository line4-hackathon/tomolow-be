package com.hackathon.tomolow.domain.group.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.hackathon.tomolow.domain.group.dto.GroupSearchResponseDto;
import com.hackathon.tomolow.domain.group.entity.Group;
import com.hackathon.tomolow.domain.group.exception.GroupErrorCode;
import com.hackathon.tomolow.domain.group.repository.GroupRepository;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.user.exception.UserErrorCode;
import com.hackathon.tomolow.domain.user.repository.UserRepository;
import com.hackathon.tomolow.domain.userGroup.entity.UserGroup;
import com.hackathon.tomolow.domain.userGroup.repository.UserGroupRepository;
import com.hackathon.tomolow.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupEnterService {

  private final GroupRepository groupRepository;
  private final UserGroupRepository userGroupRepository;
  private final UserRepository userRepository;

  public GroupSearchResponseDto searchGroup(String code) {
    Group group =
        groupRepository
            .findByCode(code)
            .orElseThrow(
                () -> new CustomException(GroupErrorCode.GROUP_NOT_FOUND, "해당 코드의 그룹이 존재하지 않습니다."));

    User creator = group.getCreator();
    long currentMemberCount = userGroupRepository.countByGroup_Id(group.getId());

    return GroupSearchResponseDto.builder()
        .groupId(group.getId())
        .groupName(group.getName())
        .creatorNickname(creator.getNickname())
        .memberCount(group.getMemberCount())
        .currentMemberCount((int) currentMemberCount)
        .seedMoney(group.getSeedMoney().setScale(0, RoundingMode.DOWN))
        .build();
  }

  @Transactional
  public Long joinGroup(Long userId, Long groupId) {
    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new CustomException(GroupErrorCode.GROUP_NOT_FOUND));
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    // 1. 그룹에 가입할 수 있는지 확인 ----------
    // 1-1. 이미 가입한 그룹인지
    if (userGroupRepository.existsByGroup_IdAndUser_Id(groupId, userId))
      throw new CustomException(GroupErrorCode.GROUP_ALREADY_JOINED);

    // 1-2. 그룹에 남은 자리가 있는지 - active하다면 멤버가 다 모인 것
    if (group.getIsActive()) throw new CustomException(GroupErrorCode.GROUP_MEMBER_LIMIT_EXCEEDED);

    // 1-3. 사용자 현금 잔액 >= 시드머니인지
    if (user.getCashBalance().compareTo(group.getSeedMoney()) >= 0)
      throw new CustomException(GroupErrorCode.GROUP_INSUFFICIENT_BALANCE);

    // 2. 그룹 가입 처리 ----------
    UserGroup userGroup =
        UserGroup.builder()
            .user(user)
            .group(group)
            .investmentBalance(BigDecimal.ZERO)
            .cashBalance(group.getSeedMoney())
            .build();

    // 2-1. 그룹 총 자산 증가, 사용자 현금 자산 감소
    group.addTotalMoney(group.getSeedMoney());
    user.subtractCashBalance(group.getSeedMoney());

    userGroupRepository.save(userGroup);

    // 3. 목표 인원 수 달성 시 그룹 활성화 처리 ----------
    long currentMemberCount = userGroupRepository.countByGroup_Id(group.getId());
    if (currentMemberCount == group.getMemberCount()) {
      group.setGroupActive(true);
      group.setGroupActivatedAt(LocalDateTime.now());
      // 스케줄러에 의해 duration만큼 지나면 종료 처리
    }

    return groupId;
  }
}
