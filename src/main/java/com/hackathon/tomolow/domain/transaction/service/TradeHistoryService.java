package com.hackathon.tomolow.domain.transaction.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackathon.tomolow.domain.transaction.dto.DailyTradeHistoryDto;
import com.hackathon.tomolow.domain.transaction.dto.TradeHistoryItemDto;
import com.hackathon.tomolow.domain.transaction.dto.TradeHistoryResponse;
import com.hackathon.tomolow.domain.transaction.entity.TradeType;
import com.hackathon.tomolow.domain.transaction.entity.Transaction;
import com.hackathon.tomolow.domain.transaction.repository.TransactionRepository;
import com.hackathon.tomolow.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TradeHistoryService {

  private final TransactionRepository transactionRepository;

  @Transactional(readOnly = true)
  public TradeHistoryResponse getHistory(User user, LocalDate startDate, LocalDate endDate) {
    // [1] 날짜 → LocalDateTime 범위로 변환 (끝 날짜 포함)
    LocalDateTime start = startDate.atStartOfDay();
    LocalDateTime end = endDate.plusDays(1).atStartOfDay(); // [start, end) 구간

    // [2] DB에서 해당 기간 거래 내역 조회 (최신순)
    List<Transaction> txs =
        transactionRepository.findAllByUserAndCreatedAtBetweenOrderByCreatedAtDesc(
            user, start, end);

    // [3] 요약 값 계산
    BigDecimal totalBuy = BigDecimal.ZERO;
    BigDecimal totalSell = BigDecimal.ZERO;

    List<TradeHistoryItemDto> items = new ArrayList<>();

    for (Transaction tx : txs) {
      BigDecimal amount = tx.getPrice().multiply(BigDecimal.valueOf(tx.getQuantity())); // 가격 * 수량

      if (tx.getTradeType() == TradeType.BUY) {
        totalBuy = totalBuy.add(amount);
      } else {
        totalSell = totalSell.add(amount);
      }

      items.add(
          TradeHistoryItemDto.builder()
              .tradedAt(tx.getCreatedAt())
              .name(tx.getMarket().getName())
              .symbol(tx.getMarket().getSymbol())
              .price(tx.getPrice())
              .quantity(tx.getQuantity())
              .tradeType(tx.getTradeType())
              .amount(amount)
              .build());
    }

    BigDecimal periodPnl = totalSell.subtract(totalBuy); // 매도 - 매수

    BigDecimal pnlRate =
        (totalBuy.signum() == 0)
            ? BigDecimal.ZERO
            : periodPnl.divide(totalBuy, 4, RoundingMode.HALF_UP); // 예: -0.0245

    // [4] 날짜별 그룹핑 (최신 날짜 순)
    Map<LocalDate, List<TradeHistoryItemDto>> byDate =
        items.stream()
            .collect(
                Collectors.groupingBy(
                    i -> i.getTradedAt().toLocalDate(), LinkedHashMap::new, Collectors.toList()));

    List<DailyTradeHistoryDto> days =
        byDate.entrySet().stream()
            .sorted(Map.Entry.<LocalDate, List<TradeHistoryItemDto>>comparingByKey().reversed())
            .map(e -> DailyTradeHistoryDto.builder().date(e.getKey()).items(e.getValue()).build())
            .toList();

    return TradeHistoryResponse.builder()
        .periodPnlAmount(periodPnl)
        .periodPnlRate(pnlRate)
        .totalBuyAmount(totalBuy)
        .totalSellAmount(totalSell)
        .days(days)
        .build();
  }

  @Transactional(readOnly = true)
  public TradeHistoryResponse getDefaultHistory(User user) {
    // 1) 해당 유저의 첫 거래 찾기
    var firstTxOpt = transactionRepository.findFirstByUserOrderByCreatedAtAsc(user);

    // 2) 거래가 아예 없으면 빈 응답 반환
    if (firstTxOpt.isEmpty()) {
      return TradeHistoryResponse.builder()
          .periodPnlAmount(BigDecimal.ZERO)
          .periodPnlRate(BigDecimal.ZERO)
          .totalBuyAmount(BigDecimal.ZERO)
          .totalSellAmount(BigDecimal.ZERO)
          .days(List.of())
          .build();
    }

    LocalDate firstDate = firstTxOpt.get().getCreatedAt().toLocalDate();
    LocalDate today = LocalDate.now();

    // 🌟 한 줄 핵심 로직: "첫 거래일 ~ 오늘" 범위로 기존 메서드 재사용
    return getHistory(user, firstDate, today);
  }
}
