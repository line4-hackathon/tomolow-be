package com.hackathon.tomolow.domain.userMarketHolding.controller;

import com.hackathon.tomolow.domain.userMarketHolding.dto.HoldingsResponse;
import com.hackathon.tomolow.domain.userMarketHolding.service.HoldingQueryService;
import com.hackathon.tomolow.global.response.BaseResponse;
import com.hackathon.tomolow.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserMarketHoldingController {

  private final HoldingQueryService holdingQueryService;

  @GetMapping("/home/assets/my")
  @Operation(summary = "홈화면 진입 시 현재 내 자산 조회 + 내 보유 종목 정보 + 총 손익 조회(실시간 가격 반영)")
  public ResponseEntity<BaseResponse<HoldingsResponse>> myHoldings(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    var body = holdingQueryService.getMyHoldings(userDetails.getUser());
    return ResponseEntity.ok(BaseResponse.success("OK", body));
  }
}
