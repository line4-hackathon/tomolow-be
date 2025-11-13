package com.hackathon.tomolow.domain.userMarketHolding.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.tomolow.domain.userMarketHolding.dto.HoldingStatusResponse;
import com.hackathon.tomolow.domain.userMarketHolding.dto.HoldingsResponse;
import com.hackathon.tomolow.domain.userMarketHolding.service.HoldingQueryService;
import com.hackathon.tomolow.domain.userMarketHolding.service.UserMarketHoldingService;
import com.hackathon.tomolow.global.response.BaseResponse;
import com.hackathon.tomolow.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserMarketHoldingController {

  private final HoldingQueryService holdingQueryService;
  private final UserMarketHoldingService userMarketHoldingService;

  @GetMapping("/home/assets/my")
  @Operation(summary = "홈화면 진입 시 현재 내 자산 조회 + 내 보유 종목 정보 + 총 손익 조회(실시간 가격 반영)")
  public ResponseEntity<BaseResponse<HoldingsResponse>> myHoldings(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    var body = holdingQueryService.getMyHoldings(userDetails.getUser());
    return ResponseEntity.ok(BaseResponse.success("OK", body));
  }

  @Operation(summary = "특정 종목 보유 여부 조회", description = "현재 로그인한 사용자가 해당 종목을 보유 중인지 여부를 반환합니다.")
  @GetMapping("/market/{marketId}/holding")
  public ResponseEntity<?> checkHolding(
      @PathVariable Long marketId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long userId = userDetails.getUser().getId();
    boolean hasHolding = userMarketHoldingService.hasHolding(userId, marketId);

    return ResponseEntity.ok(
        BaseResponse.success("보유 여부 조회 성공", new HoldingStatusResponse(hasHolding)));
  }
}
