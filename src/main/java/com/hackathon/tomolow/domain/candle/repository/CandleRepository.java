package com.hackathon.tomolow.domain.candle.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.tomolow.domain.candle.entity.Candle;
import com.hackathon.tomolow.domain.market.entity.Market;

public interface CandleRepository extends JpaRepository<Candle, Long> {

  Optional<Candle> findByMarketAndStartTimeAndIntervalMin(
      Market market, LocalDateTime start, Long intervalMin);

  // 최근 2개 일봉(가장 최근=오늘/전일)이면 [0]=최근, [1]=전일
  List<Candle> findTop2ByMarketAndIntervalMinOrderByStartTimeDesc(Market market, Long intervalMin);

  // 저장은 1일봉만 하기로 했으므로 interval_min = 1440 고정
  List<Candle> findByMarket_SymbolAndIntervalMinAndStartTimeBetweenOrderByStartTimeAsc(
      String symbol, Long intervalMin, LocalDateTime from, LocalDateTime to);

  // 기간 제한 없이 전부 (차트 최신 limit개만 잘라 쓰기)
  List<Candle> findByMarket_SymbolAndIntervalMinOrderByStartTimeAsc(
      String symbol, Long intervalMin);
}
