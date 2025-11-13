package com.hackathon.tomolow.domain.transaction.controller;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.tomolow.domain.transaction.dto.InfoResponseDto;
import com.hackathon.tomolow.domain.transaction.dto.OrderRequestDto;
import com.hackathon.tomolow.domain.transaction.dto.TradeHistoryResponse;
import com.hackathon.tomolow.domain.transaction.service.LimitTransactionService;
import com.hackathon.tomolow.domain.transaction.service.MarketTransactionService;
import com.hackathon.tomolow.domain.transaction.service.TradeHistoryService;
import com.hackathon.tomolow.domain.transaction.service.TransactionInfoService;
import com.hackathon.tomolow.global.response.BaseResponse;
import com.hackathon.tomolow.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api")
@Tag(name = "Buy/Sell", description = "개인 매수/매도 관련 API")
public class TransactionController {

  private final LimitTransactionService limitTransactionService;
  private final MarketTransactionService marketTransactionService;
  private final TransactionInfoService transactionInfoService;
  private final TradeHistoryService tradeHistoryService;

  @PostMapping("/buy/limit/{marketId}")
  @Operation(summary = "지정가 매수", description = "지정가 매수를 위한 API")
  public ResponseEntity<BaseResponse<?>> limitBuyOrder(
      @PathVariable Long marketId,
      @Valid @RequestBody OrderRequestDto orderRequestDto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long userId = userDetails.getUser().getId();
    String buyOrderId = limitTransactionService.limitBuy(userId, marketId, orderRequestDto);
    return ResponseEntity.ok(BaseResponse.success(buyOrderId));
  }

  @PostMapping("/sell/limit/{marketId}")
  @Operation(summary = "지정가 매도", description = "지정가 매도를 위한 API")
  public ResponseEntity<BaseResponse<?>> limitSellOrder(
      @PathVariable Long marketId,
      @Valid @RequestBody OrderRequestDto orderRequestDto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long userId = userDetails.getUser().getId();
    String sellOrderId = limitTransactionService.limitSell(userId, marketId, orderRequestDto);
    return ResponseEntity.ok(BaseResponse.success(sellOrderId));
  }

  @PostMapping("/buy/market/{marketId}")
  @Operation(summary = "시장가 매수", description = "시장가 매수를 위한 API")
  public ResponseEntity<BaseResponse<?>> marketBuyOrder(
      @PathVariable Long marketId, @Valid @RequestBody OrderRequestDto orderRequestDto) {
    marketTransactionService.marketBuy(marketId, orderRequestDto);
    return ResponseEntity.ok(BaseResponse.success(null));
  }

  @PostMapping("/sell/market/{marketId}")
  @Operation(summary = "시장가 매도", description = "시장가 매도를 위한 API")
  public ResponseEntity<BaseResponse<?>> marketSellOrder(
      @PathVariable Long marketId, @Valid @RequestBody OrderRequestDto orderRequestDto) {
    marketTransactionService.marketSell(marketId, orderRequestDto);
    return ResponseEntity.ok(BaseResponse.success(null));
  }

  @GetMapping("/buy/market/{marketId}")
  @Operation(
      summary = "시장가 거래 - 시장가 / 최대 매수 수량 / 보유 현금 조회",
      description = "시장가 거래 - 시장가 / 최대 매수 수량 / 보유 현금 조회를 위한 API")
  public ResponseEntity<BaseResponse<?>> getMarketBuyInfo(
      @PathVariable Long marketId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    Long userId = customUserDetails.getUser().getId();
    InfoResponseDto marketBuyInfo = transactionInfoService.getMarketBuyInfo(userId, marketId);
    return ResponseEntity.ok(BaseResponse.success(marketBuyInfo));
  }

  @GetMapping("/buy/limit/{marketId}")
  @Operation(
      summary = "지정가 거래 - 최대 매수 수량 / 보유 현금 조회",
      description = "지정가 거래 - 최대 매수 수량 / 보유 현금 조회를 위한 API")
  public ResponseEntity<BaseResponse<?>> getLimitBuyInfo(
      @PathVariable Long marketId,
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @RequestParam @NotNull BigDecimal price) {
    Long userId = customUserDetails.getUser().getId();
    InfoResponseDto limitBuyInfo = transactionInfoService.getLimitBuyInfo(userId, marketId, price);
    return ResponseEntity.ok(BaseResponse.success(limitBuyInfo));
  }

  @GetMapping("/sell/{marketId}")
  @Operation(summary = "시장가 및 최대 매도 수량 조회", description = "지정가, 시장가 거래 - 시장가 및 최대 매도 수량 조회를 위한 API")
  public ResponseEntity<BaseResponse<?>> getSellInfo(
      @PathVariable Long marketId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    Long userId = customUserDetails.getUser().getId();
    InfoResponseDto sellInfo = transactionInfoService.getSellInfo(userId, marketId);
    return ResponseEntity.ok(BaseResponse.success(sellInfo));
  }

  @Operation(summary = "기간별 거래내역 조회", description = "시작일~종료일 동안의 매수/매도 내역과 기간 손익 요약을 조회합니다.")
  @GetMapping("/transactions/history")
  public ResponseEntity<BaseResponse<TradeHistoryResponse>> getTradeHistory(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

    TradeHistoryResponse resp =
        tradeHistoryService.getHistory(userDetails.getUser(), startDate, endDate);

    return ResponseEntity.ok(BaseResponse.success("거래내역 조회 성공", resp));
  }

  @Operation(summary = "기본 범위 거래내역 조회", description = "해당 유저의 첫 거래일 ~ 오늘 날짜까지의 거래내역과 손익을 조회합니다.")
  @GetMapping("/transactions/history/default")
  public ResponseEntity<BaseResponse<TradeHistoryResponse>> getDefaultTradeHistory(
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    TradeHistoryResponse resp = tradeHistoryService.getDefaultHistory(userDetails.getUser());

    return ResponseEntity.ok(BaseResponse.success("거래내역(기본 범위) 조회 성공", resp));
  }
}
