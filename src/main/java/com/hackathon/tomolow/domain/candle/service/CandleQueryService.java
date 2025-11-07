package com.hackathon.tomolow.domain.candle.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.hackathon.tomolow.domain.candle.dto.request.CandleTf;
import com.hackathon.tomolow.domain.candle.dto.response.CandlePointResponse;
import com.hackathon.tomolow.domain.candle.entity.Candle;
import com.hackathon.tomolow.domain.candle.repository.CandleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CandleQueryService {

  private final CandleRepository candleRepo;

  private static final long DAILY = 1440L; // 1일봉만 DB 저장

  // KST 기준으로 보정
  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  public List<CandlePointResponse> getCandles(String symbol, CandleTf tf, Integer limit) {
    // 1) 전체 1일봉 가져오기 (필요시 날짜/갯수로 줄여도 됨)
    List<Candle> daily =
        candleRepo.findByMarket_SymbolAndIntervalMinOrderByStartTimeAsc(symbol, DAILY);

    if (daily.isEmpty()) {
      return List.of();
    }

    // 2) KST 로컬데이트 기준으로 변환해 둠
    List<Candle> normalized = daily;

    // 3) 타임프레임별 집계
    List<CandlePointResponse> out;
    switch (tf) {
      case D1 -> out = mapDaily(normalized);
      case W1 -> out = aggregateWeekly(normalized);
      case M1 -> out = aggregateMonthly(normalized, 1);
      case M3 -> out = aggregateMonthly(normalized, 3);
      case Y1 -> out = aggregateYearly(normalized);
      default -> out = mapDaily(normalized);
    }

    // 4) 최신에서 limit개만 반환 (기본: 전체)
    if (limit != null && limit > 0 && out.size() > limit) {
      return out.subList(out.size() - limit, out.size());
    }
    return out;
  }

  private List<CandlePointResponse> mapDaily(List<Candle> daily) {
    return daily.stream()
        .map(
            c ->
                CandlePointResponse.builder()
                    .startTime(toKst(c.getStartTime()))
                    .endTime(toKst(c.getStartTime()).plusDays(1).minusSeconds(1))
                    .open(c.getOpenPrice())
                    .high(c.getHighPrice())
                    .low(c.getLowPrice())
                    .close(c.getClosePrice())
                    .volume(c.getVolume())
                    .build())
        .toList();
  }

  /** ISO 주차(월요일 시작) 기준 집계 */
  private List<CandlePointResponse> aggregateWeekly(List<Candle> daily) {
    WeekFields wf = WeekFields.ISO; // 월요일 시작
    Map<String, List<Candle>> grouped =
        daily.stream()
            .collect(
                Collectors.groupingBy(
                    c -> {
                      LocalDate d = toKst(c.getStartTime()).toLocalDate();
                      int y = d.get(wf.weekBasedYear());
                      int w = d.get(wf.weekOfWeekBasedYear());
                      return y + "-W" + w;
                    },
                    LinkedHashMap::new,
                    Collectors.toList()));

    List<CandlePointResponse> out = new ArrayList<>();
    for (List<Candle> bucket : grouped.values()) {
      out.add(mergeBucket(bucket));
    }
    return out;
  }

  /** monthSpan=1(월봉), 3(3개월봉) */
  private List<CandlePointResponse> aggregateMonthly(List<Candle> daily, int monthSpan) {
    Map<String, List<Candle>> grouped =
        daily.stream()
            .collect(
                Collectors.groupingBy(
                    c -> {
                      LocalDate d = toKst(c.getStartTime()).toLocalDate();
                      int spanIdx = (d.getMonthValue() - 1) / monthSpan; // 0~3
                      int spanMonthStart = (spanIdx * monthSpan) + 1; // 1,4,7,10 (monthSpan=3일 때)
                      YearMonth anchor = YearMonth.of(d.getYear(), spanMonthStart);
                      return anchor.toString() + "/span" + monthSpan; // 예: 2025-01/span3
                    },
                    LinkedHashMap::new,
                    Collectors.toList()));

    List<CandlePointResponse> out = new ArrayList<>();
    for (Map.Entry<String, List<Candle>> e : grouped.entrySet()) {
      List<Candle> bucket = e.getValue();
      // start/end 계산
      LocalDateTime start =
          toKst(bucket.get(0).getStartTime())
              .withDayOfMonth(1)
              .withHour(0)
              .withMinute(0)
              .withSecond(0)
              .withNano(0);
      LocalDateTime end = start.plusMonths(monthSpan).minusSeconds(1);
      out.add(mergeBucket(bucket, start, end));
    }
    return out;
  }

  /** yaerSpan=1(년봉) */
  private List<CandlePointResponse> aggregateYearly(List<Candle> daily) {
    Map<Integer, List<Candle>> grouped =
        daily.stream()
            .collect(
                Collectors.groupingBy(
                    c -> {
                      LocalDate d = toKst(c.getStartTime()).toLocalDate();
                      return d.getYear(); // 연도별 그룹
                    },
                    LinkedHashMap::new,
                    Collectors.toList()));

    List<CandlePointResponse> out = new ArrayList<>();
    for (Map.Entry<Integer, List<Candle>> e : grouped.entrySet()) {
      int year = e.getKey();
      List<Candle> bucket = e.getValue();
      LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0);
      LocalDateTime end = start.plusYears(1).minusSeconds(1);
      out.add(mergeBucket(bucket, start, end));
    }
    return out;
  }

  /** 버킷(연속 일봉들)을 하나의 캔들로 머지 */
  private CandlePointResponse mergeBucket(List<Candle> bucket) {
    bucket.sort(Comparator.comparing(Candle::getStartTime));
    LocalDateTime s = toKst(bucket.get(0).getStartTime());
    // endTime은 버킷 마지막 날의 23:59:59 로 표시
    LocalDateTime e =
        toKst(bucket.get(bucket.size() - 1).getStartTime())
            .withHour(23)
            .withMinute(59)
            .withSecond(59);
    return mergeBucket(bucket, s, e);
  }

  private CandlePointResponse mergeBucket(
      List<Candle> bucket, LocalDateTime start, LocalDateTime end) {
    bucket.sort(Comparator.comparing(Candle::getStartTime));
    var open = bucket.get(0).getOpenPrice();
    var close = bucket.get(bucket.size() - 1).getClosePrice();
    var high = bucket.stream().map(Candle::getHighPrice).max(BigDecimal::compareTo).orElse(open);
    var low = bucket.stream().map(Candle::getLowPrice).min(BigDecimal::compareTo).orElse(open);
    var vol = bucket.stream().map(Candle::getVolume).reduce(BigDecimal.ZERO, BigDecimal::add);

    return CandlePointResponse.builder()
        .startTime(start)
        .endTime(end)
        .open(open)
        .high(high)
        .low(low)
        .close(close)
        .volume(vol)
        .build();
  }

  private LocalDateTime toKst(LocalDateTime utcLike) {
    // DB가 LocalDateTime(UTC 기준 저장 or KST 저장)이라면, 여기서는 “표시 기준”만 KST로 맞추는 용도
    return ZonedDateTime.of(utcLike, ZoneId.systemDefault())
        .withZoneSameInstant(KST)
        .toLocalDateTime();
  }
}
