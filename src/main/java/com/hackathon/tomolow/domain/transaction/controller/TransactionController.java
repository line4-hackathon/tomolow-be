package com.hackathon.tomolow.domain.transaction.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.tomolow.domain.transaction.dto.OrderRequestDto;
import com.hackathon.tomolow.domain.transaction.service.LimitTransactionService;
import com.hackathon.tomolow.global.response.BaseResponse;
import com.hackathon.tomolow.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Buy/Sell", description = "개인 매수/매도 관련 API")
public class TransactionController {

  private final LimitTransactionService limitTransactionService;

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
}
