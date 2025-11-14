package com.hackathon.tomolow.domain.transaction.service;

import com.hackathon.tomolow.domain.transaction.dto.DailyTradeHistoryDto;
import com.hackathon.tomolow.domain.transaction.dto.TradeHistoryItemDto;
import com.hackathon.tomolow.domain.transaction.dto.TradeHistoryResponse;
import com.hackathon.tomolow.domain.transaction.entity.TradeType;
import com.hackathon.tomolow.domain.transaction.entity.Transaction;
import com.hackathon.tomolow.domain.transaction.repository.TransactionRepository;
import com.hackathon.tomolow.domain.user.entity.User;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        transactionRepository.findAllByUserAndCreatedAtBetweenOrderByCreatedAtAsc(user, start, end);

    // [3] 요약 값 계산
    BigDecimal totalBuy = BigDecimal.ZERO;
    BigDecimal totalSell = BigDecimal.ZERO;
    BigDecimal realizedPnl = BigDecimal.ZERO; // 실제 손익

    // 종목별 포지션 상태 (이동평균법)
    // key: marketId
    Map<Long, BigDecimal> positionQtyMap = new HashMap<>();
    Map<Long, BigDecimal> positionCostMap = new HashMap<>();

    List<TradeHistoryItemDto> items = new ArrayList<>();

    for (Transaction tx : txs) {
      Long marketId = tx.getMarket().getId();
      BigDecimal price = tx.getPrice();
      BigDecimal qty = BigDecimal.valueOf(tx.getQuantity());
      BigDecimal amount = price.multiply(qty); // 가격 * 수량

      // 거래 내역 DTO는 기존과 동일하게 쌓아둔다 (UI용)
      items.add(
          TradeHistoryItemDto.builder()
              .tradedAt(tx.getCreatedAt())
              .name(tx.getMarket().getName())
              .symbol(tx.getMarket().getSymbol())
              .price(price)
              .quantity(tx.getQuantity())
              .tradeType(tx.getTradeType())
              .amount(amount)
              .build());

      BigDecimal currentQty = positionQtyMap.getOrDefault(marketId, BigDecimal.ZERO);
      BigDecimal currentCost = positionCostMap.getOrDefault(marketId, BigDecimal.ZERO);

      if (tx.getTradeType() == TradeType.BUY) {
        // 매수: 총 매수금액 + 포지션 반영
        totalBuy = totalBuy.add(amount);

        BigDecimal newQty = currentQty.add(qty);
        BigDecimal newCost = currentCost.add(amount);

        positionQtyMap.put(marketId, newQty);
        positionCostMap.put(marketId, newCost);

      } else if (tx.getTradeType() == TradeType.SELL) {
        // 매도: 총 매도금액
        totalSell = totalSell.add(amount);

        // 보유 물량이 있을 때만 손익 계산
        if (currentQty.compareTo(BigDecimal.ZERO) > 0) {
          // 평단가 = 현재까지의 총 원가 / 보유 수량
          BigDecimal avgCost = currentCost.divide(currentQty, 8, RoundingMode.HALF_UP); // 소수점 넉넉히

          BigDecimal sellQty = qty;

          // 만약 매도 수량 > 보유 수량이면, 보유 수량까지만 손익 계산
          if (sellQty.compareTo(currentQty) > 0) {
            sellQty = currentQty;
          }

          BigDecimal costForSell = avgCost.multiply(sellQty);
          BigDecimal sellAmountForPnl = price.multiply(sellQty);

          BigDecimal pnl = sellAmountForPnl.subtract(costForSell);
          realizedPnl = realizedPnl.add(pnl);

          // 포지션 업데이트
          BigDecimal newQty = currentQty.subtract(sellQty);
          BigDecimal newCost = currentCost.subtract(costForSell);

          if (newQty.compareTo(BigDecimal.ZERO) <= 0) {
            newQty = BigDecimal.ZERO;
            newCost = BigDecimal.ZERO;
          }

          positionQtyMap.put(marketId, newQty);
          positionCostMap.put(marketId, newCost);
        }
        // 보유 수량이 0인데 매도한 경우는, 원가를 알 수 없으니 손익 0으로 처리 (무시)
      }
    }

    BigDecimal periodPnl = realizedPnl; // 이제는 "실제 실현 손익"

    BigDecimal pnlRate =
        (totalBuy.signum() == 0)
            ? BigDecimal.ZERO
            : periodPnl.divide(totalBuy, 4, RoundingMode.HALF_UP); // 예: -0.0245

    // [4] 날짜별 그룹핑 (최신 날짜 순, 각 날짜 내부는 거래 최신순)
    Map<LocalDate, List<TradeHistoryItemDto>> byDate =
        items.stream()
            .collect(
                Collectors.groupingBy(
                    i -> i.getTradedAt().toLocalDate(), LinkedHashMap::new, Collectors.toList()));

    List<DailyTradeHistoryDto> days =
        byDate.entrySet().stream()
            // 날짜 기준 최신 날짜 순
            .sorted(Map.Entry.<LocalDate, List<TradeHistoryItemDto>>comparingByKey().reversed())
            .map(
                e -> {
                  // 하루 안에서도 최신 거래가 위로 오도록 정렬
                  List<TradeHistoryItemDto> sortedItems =
                      e.getValue().stream()
                          .sorted(Comparator.comparing(TradeHistoryItemDto::getTradedAt).reversed())
                          .toList();
                  return DailyTradeHistoryDto.builder().date(e.getKey()).items(sortedItems).build();
                })
            .toList();

    return TradeHistoryResponse.builder()
        .periodPnlAmount(periodPnl) // 매도 기준 실현 손익 합계
        .periodPnlRate(pnlRate) // 실현 손익 / 기간 내 총 매수금액
        .totalBuyAmount(totalBuy)
        .totalSellAmount(totalSell)
        .days(days)
        .build();
  }

  @Transactional(readOnly = true)
  public TradeHistoryResponse getDefaultHistory(User user) {
    var firstTxOpt = transactionRepository.findFirstByUserOrderByCreatedAtAsc(user);

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

    // "첫 거래일 ~ 오늘" 범위로 기존 메서드 재사용
    return getHistory(user, firstDate, today);
  }
}
