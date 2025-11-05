package com.hackathon.tomolow.domain.userGroupStockHolding.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.tomolow.domain.userGroupStockHolding.entity.UserGroupMarketHolding;

public interface UserGroupMarketHoldingRepository
    extends JpaRepository<UserGroupMarketHolding, Long> {}
