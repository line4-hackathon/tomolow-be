package com.hackathon.tomolow.domain.userGroupStockHolding.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.tomolow.domain.userGroupStockHolding.entity.UserGroupStockHolding;

public interface UserGroupStockHoldingRepository
    extends JpaRepository<UserGroupStockHolding, Long> {}
