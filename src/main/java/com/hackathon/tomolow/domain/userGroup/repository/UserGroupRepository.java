package com.hackathon.tomolow.domain.userGroup.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hackathon.tomolow.domain.group.entity.Group;
import com.hackathon.tomolow.domain.userGroup.entity.UserGroup;

public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {
  long countByGroup_Id(Long groupId);

  boolean existsByGroup_IdAndUser_Id(Long groupId, Long userId);

  List<UserGroup> findByUser_IdAndGroup_IsActive(Long userId, Boolean active);

  @Query(
      "SELECT ug FROM UserGroup ug "
          + "WHERE ug.user.id = :userId "
          + "AND ug.group.isActive = false "
          + "AND ug.group.endAt <= :now")
  List<UserGroup> findExpiredGroupsByUser(Long userId, LocalDateTime now);

  @Query(
      "SELECT ug FROM UserGroup ug "
          + "WHERE ug.user.id = :userId "
          + "AND ug.group.activatedAt IS NULL")
  List<UserGroup> findJoinableGroupsByUser(Long userId);

  @Query(
      """
    SELECT g FROM Group g
    WHERE g.activatedAt IS NULL
    AND g.id NOT IN (
        SELECT ug.group.id FROM UserGroup ug WHERE ug.user.id = :userId
    )
    """)
  List<Group> findJoinableGroupsExceptMine(Long userId);

  Optional<List<UserGroup>> findByGroup_Id(Long groupId);
}
