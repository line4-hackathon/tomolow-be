package com.hackathon.tomolow.domain.candle.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.tomolow.domain.candle.entity.Candle;

public interface CandleRepository extends JpaRepository<Candle, Long> {}
