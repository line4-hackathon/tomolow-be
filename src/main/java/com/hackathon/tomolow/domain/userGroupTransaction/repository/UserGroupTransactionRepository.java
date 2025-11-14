package com.hackathon.tomolow.domain.userGroupTransaction.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.tomolow.domain.userGroup.entity.UserGroup;
import com.hackathon.tomolow.domain.userGroupTransaction.entity.UserGroupTransaction;

public interface UserGroupTransactionRepository extends JpaRepository<UserGroupTransaction, Long> {
  boolean existsByUserGroup_Group_Id(Long groupId);

  // 기간별 거래내역 (최신순)
  List<UserGroupTransaction> findAllByUserGroupAndCreatedAtBetweenOrderByCreatedAtDesc(
      UserGroup userGroup, LocalDateTime start, LocalDateTime end);

  // 해당 UserGroup의 첫 거래 하나 (createdAt 오름차순)
  Optional<UserGroupTransaction> findFirstByUserGroupOrderByCreatedAtAsc(UserGroup userGroup);

  List<UserGroupTransaction> findAllByUserGroupAndCreatedAtBetweenOrderByCreatedAtAsc(
      UserGroup userGroup, LocalDateTime start, LocalDateTime end);
}
