package com.hackathon.tomolow.domain.transaction.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.tomolow.domain.transaction.dto.OrderRequestDto;
import com.hackathon.tomolow.domain.transaction.service.LimitTransactionService;
import com.hackathon.tomolow.domain.transaction.service.MarketTransactionService;
import com.hackathon.tomolow.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Buy/Sell", description = "개인 매수/매도 관련 API")
public class TransactionController {

  private final LimitTransactionService limitTransactionService;
  private final MarketTransactionService marketTransactionService;

  @PostMapping("/buy/limit/{marketId}")
  @Operation(summary = "지정가 매수", description = "지정가 매수를 위한 API")
  public ResponseEntity<BaseResponse<?>> limitBuyOrder(
      @PathVariable Long marketId, @Valid @RequestBody OrderRequestDto orderRequestDto) {
    String buyOrderId = limitTransactionService.limitBuy(marketId, orderRequestDto);
    return ResponseEntity.ok(BaseResponse.success(buyOrderId));
  }

  @PostMapping("/sell/limit/{marketId}")
  @Operation(summary = "지정가 매도", description = "지정가 매도를 위한 API")
  public ResponseEntity<BaseResponse<?>> limitSellOrder(
      @PathVariable Long marketId, @Valid @RequestBody OrderRequestDto orderRequestDto) {
    String sellOrderId = limitTransactionService.limitSell(marketId, orderRequestDto);
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
}
