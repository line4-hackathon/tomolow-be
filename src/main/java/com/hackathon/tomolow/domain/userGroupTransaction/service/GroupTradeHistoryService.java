package com.hackathon.tomolow.domain.userGroupTransaction.service;

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
import com.hackathon.tomolow.domain.userGroup.entity.UserGroup;
import com.hackathon.tomolow.domain.userGroupTransaction.entity.UserGroupTransaction;
import com.hackathon.tomolow.domain.userGroupTransaction.repository.UserGroupTransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupTradeHistoryService {

  private final UserGroupTransactionRepository userGroupTransactionRepository;
  private final GroupOrderInfoService groupOrderInfoService;

  /** 🔹 특정 그룹에서의 내 거래내역 (기간 지정) */
  @Transactional(readOnly = true)
  public TradeHistoryResponse getHistory(
      Long userId, Long groupId, LocalDate startDate, LocalDate endDate) {

    // 1) UserGroup 조회 (이미 다른 서비스에서 쓰던 헬퍼 재사용)
    UserGroup userGroup = groupOrderInfoService.getUserGroup(userId, groupId);

    // 2) 날짜 범위를 LocalDateTime으로 변환  [start, end)
    LocalDateTime start = startDate.atStartOfDay();
    LocalDateTime end = endDate.plusDays(1).atStartOfDay();

    // 3) 해당 그룹 내 내 거래내역 조회 (최신순)
    List<UserGroupTransaction> txs =
        userGroupTransactionRepository.findAllByUserGroupAndCreatedAtBetweenOrderByCreatedAtDesc(
            userGroup, start, end);

    BigDecimal totalBuy = BigDecimal.ZERO;
    BigDecimal totalSell = BigDecimal.ZERO;

    List<TradeHistoryItemDto> items = new ArrayList<>();

    for (UserGroupTransaction tx : txs) {
      BigDecimal amount = tx.getPrice().multiply(BigDecimal.valueOf(tx.getQuantity()));

      if (tx.getTradeType() == TradeType.BUY) {
        totalBuy = totalBuy.add(amount);
      } else {
        totalSell = totalSell.add(amount);
      }

      items.add(
          TradeHistoryItemDto.builder()
              .tradedAt(tx.getCreatedAt()) // ✅ 개인과 동일: tradedAt
              .name(tx.getMarket().getName())
              .symbol(tx.getMarket().getSymbol())
              .price(tx.getPrice())
              .quantity(tx.getQuantity())
              .tradeType(tx.getTradeType())
              .amount(amount)
              .build());
    }

    BigDecimal periodPnl = totalSell.subtract(totalBuy);

    BigDecimal pnlRate =
        (totalBuy.signum() == 0)
            ? BigDecimal.ZERO
            : periodPnl.divide(totalBuy, 4, RoundingMode.HALF_UP);

    // 4) 날짜별 그룹핑 (최신 날짜 순)
    Map<LocalDate, List<TradeHistoryItemDto>> byDate =
        items.stream()
            .collect(
                Collectors.groupingBy(
                    i -> i.getTradedAt().toLocalDate(), // ✅ tradedAt 기준
                    LinkedHashMap::new,
                    Collectors.toList()));

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

  /** 🔹 그룹 내 기본 범위(첫 거래일 ~ 오늘) */
  @Transactional(readOnly = true)
  public TradeHistoryResponse getDefaultHistory(Long userId, Long groupId) {

    UserGroup userGroup = groupOrderInfoService.getUserGroup(userId, groupId);

    var firstTxOpt =
        userGroupTransactionRepository.findFirstByUserGroupOrderByCreatedAtAsc(userGroup);

    // 거래가 없으면 빈 응답
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

    // 🌟 개인과 똑같이: "첫 거래일 ~ 오늘" 범위로 재사용
    return getHistory(userId, groupId, firstDate, today);
  }
}
