package com.hackathon.tomolow.domain.transaction.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.tomolow.domain.transaction.dto.OrderRequestDto;
import com.hackathon.tomolow.domain.transaction.service.TransactionService;
import com.hackathon.tomolow.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Buy/Sell", description = "개인 매수/매도 관련 API")
public class TransactionController {

  private final TransactionService transactionService;

  @PostMapping("/buy/{marketId}")
  @Operation(summary = "매도", description = "매도를 위한 API")
  public ResponseEntity<BaseResponse<?>> buyOrder(
      @PathVariable Long marketId, @Valid @RequestBody OrderRequestDto orderRequestDto) {
    String buyOrderId = transactionService.createBuyOrder(marketId, orderRequestDto);
    return ResponseEntity.ok(BaseResponse.success(buyOrderId));
  }

  @PostMapping("/sell/{marketId}")
  @Operation(summary = "매수", description = "매수를 위한 API")
  public ResponseEntity<BaseResponse<?>> sellOrder(
      @PathVariable Long marketId, @Valid @RequestBody OrderRequestDto orderRequestDto) {
    String sellOrderId = transactionService.createSellOrder(marketId, orderRequestDto);
    return ResponseEntity.ok(BaseResponse.success(sellOrderId));
  }
}
