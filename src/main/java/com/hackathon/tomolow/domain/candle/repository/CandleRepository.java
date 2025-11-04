package com.hackathon.tomolow.domain.candle.repository;

import com.hackathon.tomolow.domain.candle.entity.Candle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandleRepository extends JpaRepository<Candle, Long> {

}
