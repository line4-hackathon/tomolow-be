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

    // 1. 유저 잔액 부족하지 않은지 체크
    BigDecimal seedMoney = groupCreateDto.getSeedMoney();
    if (user.getCashBalance().compareTo(seedMoney) < 0)
      throw new CustomException(
          GroupErrorCode.GROUP_INSUFFICIENT_BALANCE, "잔액이 부족하여 그룹을 생성할 수 없습니다.");

    // 2. 동일 이름/코드의 그룹이 존재하지 않는지 확인
    if (groupRepository.existsByName(groupCreateDto.getName()))
      throw new CustomException(GroupErrorCode.GROUP_NAME_DUPLICATED);

    String groupCode = userId.hashCode() + UUID.randomUUID().toString();
    if (groupRepository.existsByCode(groupCode))
      throw new CustomException(
          GroupErrorCode.GROUP_CODE_DUPLICATED, "이미 존재하는 그룹 코드입니다. 코드 재생성을 위해 다시 시도해주세요.");

    // 3. 그룹 생성
    Group group =
        Group.builder()
            .name(groupCreateDto.getName())
            .duration(groupCreateDto.getDuration())
            .seedMoney(seedMoney)
            .memberCount(groupCreateDto.getMemberCount())
            .isActive(false)
            .code(groupCode)
            .totalMoney(seedMoney)
            .creator(user)
            .build();

    Group savedGroup = groupRepository.save(group);

    // 4. 그룹에 사용자 추가
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
}
