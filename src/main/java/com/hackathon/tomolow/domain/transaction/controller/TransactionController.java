package com.hackathon.tomolow.domain.transaction.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hackathon.tomolow.domain.transaction.dto.OrderRequestDto;
import com.hackathon.tomolow.domain.transaction.service.TransactionService;
import com.hackathon.tomolow.global.response.BaseResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TransactionController {

  private final TransactionService transactionService;

  @PostMapping("/buy/{stockId}")
  public ResponseEntity<BaseResponse<?>> buyOrder(
      @PathVariable Long stockId, @Valid @RequestBody OrderRequestDto orderRequestDto) {
    transactionService.createBuyOrder(stockId, orderRequestDto);
    return null;
  }

  @PostMapping("/sell/{stockId}")
  public ResponseEntity<BaseResponse<?>> sellOrder(
      @PathVariable Long stockId, @Valid @RequestBody OrderRequestDto orderRequestDto) {
    transactionService.createSellOrder(stockId, orderRequestDto);
    return null;
  }

}
