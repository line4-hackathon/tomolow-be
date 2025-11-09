package com.hackathon.tomolow.domain.userGroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.tomolow.domain.userGroup.entity.UserGroup;

public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {
  long countByGroup_Id(long groupId);
}
