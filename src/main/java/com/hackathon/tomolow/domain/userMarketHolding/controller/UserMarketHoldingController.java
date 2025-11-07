package com.hackathon.tomolow.domain.userMarketHolding.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.tomolow.domain.userMarketHolding.dto.HoldingsResponse;
import com.hackathon.tomolow.domain.userMarketHolding.service.HoldingQueryService;
import com.hackathon.tomolow.global.response.BaseResponse;
import com.hackathon.tomolow.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserMarketHoldingController {

  private final HoldingQueryService holdingQueryService;

  @GetMapping("/mypage/holdings/my")
  @Operation(summary = "내 보유 종목 + 총 손익 조회(실시간 가격 반영)")
  public ResponseEntity<BaseResponse<HoldingsResponse>> myHoldings(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    var body = holdingQueryService.getMyHoldings(userDetails.getUser());
    return ResponseEntity.ok(BaseResponse.success("OK", body));
  }
}
