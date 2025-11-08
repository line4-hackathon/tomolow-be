package com.hackathon.tomolow.domain.userInterestedMarket.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.tomolow.domain.userInterestedMarket.entity.UserInterestedMarket;

public interface UserInterestedMarketRepository extends JpaRepository<UserInterestedMarket, Long> {

  boolean existsByUser_IdAndMarket_Id(Long userId, Long marketId);

  Optional<UserInterestedMarket> findByUser_IdAndMarket_Id(Long userId, Long marketId);

  void deleteByUser_IdAndMarket_Id(Long userId, Long marketId);

  List<UserInterestedMarket> findAllByUser_IdOrderByCreatedAtDesc(Long userId);
}
