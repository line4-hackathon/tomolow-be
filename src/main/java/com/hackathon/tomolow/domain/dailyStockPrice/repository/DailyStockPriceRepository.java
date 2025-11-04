package com.hackathon.tomolow.domain.dailyStockPrice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.tomolow.domain.dailyStockPrice.entity.DailyStockPrice;

public interface DailyStockPriceRepository extends JpaRepository<DailyStockPrice, Long> {}
