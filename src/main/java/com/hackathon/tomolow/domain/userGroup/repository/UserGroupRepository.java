package com.hackathon.tomolow.domain.userGroup.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.tomolow.domain.userGroup.entity.UserGroup;

public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {
  long countByGroup_Id(Long groupId);

  boolean existsByGroup_IdAndUser_Id(Long groupId, Long userId);

  List<UserGroup> findByUser_IdAndGroup_IsActive(Long userId, Boolean active);
}
