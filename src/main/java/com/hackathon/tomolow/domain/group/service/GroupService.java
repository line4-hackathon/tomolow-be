package com.hackathon.tomolow.domain.group.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackathon.tomolow.domain.group.dto.GroupCreateDto;
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

@Service
@RequiredArgsConstructor
public class GroupService {

  private final GroupRepository groupRepository;
  private final UserRepository userRepository;
  private final UserGroupRepository userGroupRepository;

  @Transactional
  public Long createGroup(Long userId, GroupCreateDto groupCreateDto) {

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    // 유저 잔액 부족하지 않은지 체크
    BigDecimal seedMoney = groupCreateDto.getSeedMoney();
    if (user.getCashBalance().compareTo(seedMoney) < 0)
      throw new CustomException(
          GroupErrorCode.GROUP_INSUFFICIENT_BALANCE, "잔액이 부족하여 그룹을 생성할 수 없습니다.");

    // 그룹 생성
    String groupCode = userId.hashCode() + UUID.randomUUID().toString();

    Group group =
        Group.builder()
            .name(groupCreateDto.getName())
            .duration(groupCreateDto.getDuration())
            .seedMoney(seedMoney)
            .memberCount(groupCreateDto.getMemberCount())
            .isActive(false)
            .code(groupCode)
            .totalMoney(seedMoney)
            .build();

    Group savedGroup = groupRepository.save(group);

    // 그룹에 사용자 추가
    UserGroup userGroup =
        UserGroup.builder()
            .user(user)
            .group(group)
            .investmentBalance(BigDecimal.ZERO)
            .cashBalance(seedMoney)
            .build();

    userGroupRepository.save(userGroup);

    return savedGroup.getId();
  }

  // TODO : 그룹 참가 시 시드머니 충분한지, 활성화된 상태인지, 인원수 오버하지 않는지 확인
  // TODO : 그룹 참가 시 totalMoney 업데이트, duration에 맞게 스케줄링 필요
  // TODO : 그룹 참가 시 개인 현금 감소
}
