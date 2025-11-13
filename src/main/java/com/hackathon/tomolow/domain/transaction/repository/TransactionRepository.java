package com.hackathon.tomolow.domain.transaction.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.tomolow.domain.transaction.entity.Transaction;
import com.hackathon.tomolow.domain.user.entity.User;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

  List<Transaction> findAllByUserAndCreatedAtBetweenOrderByCreatedAtDesc(
      User user, LocalDateTime start, LocalDateTime end);

  // 해당 유저의 "첫 거래" 하나만 (createdAt 오름차순)
  Optional<Transaction> findFirstByUserOrderByCreatedAtAsc(User user);
}
