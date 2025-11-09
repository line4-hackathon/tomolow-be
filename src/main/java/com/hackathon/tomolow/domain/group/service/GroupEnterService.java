package com.hackathon.tomolow.domain.group.service;

import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import com.hackathon.tomolow.domain.group.dto.GroupSearchResponseDto;
import com.hackathon.tomolow.domain.group.entity.Group;
import com.hackathon.tomolow.domain.group.exception.GroupErrorCode;
import com.hackathon.tomolow.domain.group.repository.GroupRepository;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.userGroup.repository.UserGroupRepository;
import com.hackathon.tomolow.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupEnterService {

  private final GroupRepository groupRepository;
  private final UserGroupRepository userGroupRepository;

  public GroupSearchResponseDto searchGroup(String code) {
    Group group =
        groupRepository
            .findByCode(code)
            .orElseThrow(
                () -> new CustomException(GroupErrorCode.GROUP_NOT_FOUND, "해당 코드의 그룹이 존재하지 않습니다."));

    User creator = group.getCreator();
    long currentMemberCount = userGroupRepository.countByGroup_Id(group.getId());

    return GroupSearchResponseDto.builder()
        .groupName(group.getName())
        .creatorNickname(creator.getNickname())
        .memberCount(group.getMemberCount())
        .currentMemberCount((int) currentMemberCount)
        .seedMoney(group.getSeedMoney().setScale(0, RoundingMode.DOWN))
        .build();
  }

  // TODO : 그룹 참가 시 시드머니 충분한지, 활성화된 상태인지, 인원수 오버하지 않는지 확인
  // TODO : 그룹 참가 시 totalMoney 업데이트, duration에 맞게 스케줄링 필요
  // TODO : 그룹 참가 시 개인 현금 감소, 이미 가입하지 않았는지 체크
}
