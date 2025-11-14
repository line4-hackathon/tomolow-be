package com.hackathon.tomolow.domain.userGroupTransaction.service;

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

  /** 특정 그룹에서의 내 거래내역 (기간 지정) - 실현손익 기준 */
  @Transactional(readOnly = true)
  public TradeHistoryResponse getHistory(
      Long userId, Long groupId, LocalDate startDate, LocalDate endDate) {

    // 1) UserGroup 조회 (이미 다른 서비스에서 쓰던 헬퍼 재사용)
    UserGroup userGroup = groupOrderInfoService.getUserGroup(userId, groupId);

    // 2) 날짜 범위를 LocalDateTime으로 변환  [start, end)
    LocalDateTime start = startDate.atStartOfDay();
    LocalDateTime end = endDate.plusDays(1).atStartOfDay();

    // 3) 해당 그룹 내 내 거래내역 조회 (오래된 순으로 정렬)
    List<UserGroupTransaction> txs =
        userGroupTransactionRepository.findAllByUserGroupAndCreatedAtBetweenOrderByCreatedAtAsc(
            userGroup, start, end);

    BigDecimal totalBuy = BigDecimal.ZERO;
    BigDecimal totalSell = BigDecimal.ZERO;
    BigDecimal realizedPnl = BigDecimal.ZERO; // 매도 시점 기준 실현손익

    // 종목별 포지션 상태 (이동평균법)
    Map<Long, BigDecimal> positionQtyMap = new HashMap<>();
    Map<Long, BigDecimal> positionCostMap = new HashMap<>();

    List<TradeHistoryItemDto> items = new ArrayList<>();

    for (UserGroupTransaction tx : txs) {
      Long marketId = tx.getMarket().getId();
      BigDecimal price = tx.getPrice();
      BigDecimal qty = BigDecimal.valueOf(tx.getQuantity());
      BigDecimal amount = price.multiply(qty);

      // UI에 뿌릴 개별 거래내역은 기존과 동일하게 쌓기
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

          // 매도 수량이 보유 수량보다 크면, 보유 수량까지만 손익 반영
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
        // 보유 수량 0인데 매도한 경우 → 원가를 모르는 구간이라 손익은 0으로 무시
      }
    }

    BigDecimal periodPnl = realizedPnl; // 실현손익 합계

    BigDecimal pnlRate =
        (totalBuy.signum() == 0)
            ? BigDecimal.ZERO
            : periodPnl.divide(totalBuy, 4, RoundingMode.HALF_UP);

    // 4) 날짜별 그룹핑 (날짜는 최신 날짜 순, 하루 안에서는 최신 거래 먼저)
    Map<LocalDate, List<TradeHistoryItemDto>> byDate =
        items.stream()
            .collect(
                Collectors.groupingBy(
                    i -> i.getTradedAt().toLocalDate(), LinkedHashMap::new, Collectors.toList()));

    List<DailyTradeHistoryDto> days =
        byDate.entrySet().stream()
            // 날짜 최신순
            .sorted(Map.Entry.<LocalDate, List<TradeHistoryItemDto>>comparingByKey().reversed())
            .map(
                e -> {
                  // 하루 안에서는 최신 거래가 위로 오도록 정렬
                  List<TradeHistoryItemDto> sortedItems =
                      e.getValue().stream()
                          .sorted(Comparator.comparing(TradeHistoryItemDto::getTradedAt).reversed())
                          .toList();

                  return DailyTradeHistoryDto.builder().date(e.getKey()).items(sortedItems).build();
                })
            .toList();

    return TradeHistoryResponse.builder()
        .periodPnlAmount(periodPnl) // “실제로 벌거나 잃은 돈” 합
        .periodPnlRate(pnlRate) // 실현손익 / 기간 내 총 매수금액
        .totalBuyAmount(totalBuy)
        .totalSellAmount(totalSell)
        .days(days)
        .build();
  }

  /** 그룹 내 기본 범위(첫 거래일 ~ 오늘) */
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
