package com.hackathon.tomolow.domain.market.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.tomolow.domain.market.entity.Market;

public interface MarketRepository extends JpaRepository<Market, Long> {}
