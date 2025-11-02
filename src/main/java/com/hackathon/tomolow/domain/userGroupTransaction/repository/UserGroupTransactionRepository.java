package com.hackathon.tomolow.domain.userGroupTransaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.tomolow.domain.userGroupTransaction.entity.UserGroupTransaction;

public interface UserGroupTransactionRepository extends JpaRepository<UserGroupTransaction, Long> {}
