package com.hackathon.tomolow.domain.userStockHolding.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.tomolow.domain.userStockHolding.entity.UserStockHolding;

public interface UserStockHoldingRepository extends JpaRepository<UserStockHolding, Long> {}
