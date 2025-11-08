package com.hackathon.tomolow.domain.user.controller;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.tomolow.domain.user.dto.response.TopUpResponse;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.user.service.UserService;
import com.hackathon.tomolow.global.response.BaseResponse;
import com.hackathon.tomolow.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage/cash")
@Tag(name = "User", description = "유저 관련 API")
public class UserAdController {

  private final UserService userService;

  @Operation(summary = "광고 보상 충전", description = "광고 시청 시 500,000원 지급")
  @PostMapping("/ad")
  public ResponseEntity<BaseResponse<TopUpResponse>> topUpAd(
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    User user = userDetails.getUser();
    TopUpResponse res = userService.topUpFixed(user, new BigDecimal("500000"));
    return ResponseEntity.ok(BaseResponse.success("광고 보상 충전 완료", res));
  }

  @Operation(summary = "+1,000만원 충전", description = "버튼 클릭 시 1,000만원 충전")
  @PostMapping("/10m")
  public ResponseEntity<BaseResponse<TopUpResponse>> topUp10m(
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    User user = userDetails.getUser();
    TopUpResponse res = userService.topUpFixed(user, new BigDecimal("10000000"));
    return ResponseEntity.ok(BaseResponse.success("+1,000만원 충전 완료", res));
  }

  @Operation(summary = "+3,000만원 충전", description = "버튼 클릭 시 3,000만원 충전")
  @PostMapping("/30m")
  public ResponseEntity<BaseResponse<TopUpResponse>> topUp30m(
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    User user = userDetails.getUser();
    TopUpResponse res = userService.topUpFixed(user, new BigDecimal("30000000"));
    return ResponseEntity.ok(BaseResponse.success("+3,000만원 충전 완료", res));
  }

  @Operation(summary = "+5,000만원 충전", description = "버튼 클릭 시 5,000만원 충전")
  @PostMapping("/50m")
  public ResponseEntity<BaseResponse<TopUpResponse>> topUp50m(
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    User user = userDetails.getUser();
    TopUpResponse res = userService.topUpFixed(user, new BigDecimal("50000000"));
    return ResponseEntity.ok(BaseResponse.success("+5,000만원 충전 완료", res));
  }

  @Operation(summary = "+1억원 충전", description = "버튼 클릭 시 1억원 충전")
  @PostMapping("/100m")
  public ResponseEntity<BaseResponse<TopUpResponse>> topUp100m(
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    User user = userDetails.getUser();
    TopUpResponse res = userService.topUpFixed(user, new BigDecimal("100000000"));
    return ResponseEntity.ok(BaseResponse.success("+1억원 충전 완료", res));
  }
}
