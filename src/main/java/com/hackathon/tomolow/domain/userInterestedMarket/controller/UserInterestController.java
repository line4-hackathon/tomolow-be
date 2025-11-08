package com.hackathon.tomolow.domain.userInterestedMarket.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.tomolow.domain.userInterestedMarket.dto.InterestToggleResponse;
import com.hackathon.tomolow.domain.userInterestedMarket.dto.InterestedMarketCard;
import com.hackathon.tomolow.domain.userInterestedMarket.dto.InterestedMarketListResponse;
import com.hackathon.tomolow.domain.userInterestedMarket.service.UserInterestService;
import com.hackathon.tomolow.global.response.BaseResponse;
import com.hackathon.tomolow.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interests")
@Tag(name = "관심(하트)", description = "관심 마켓 등록/해제 및 조회 API")
public class UserInterestController {

  private final UserInterestService userInterestService;

  @PostMapping("/markets/{marketId}/toggle")
  @Operation(summary = "하트 토글", description = "해당 마켓을 관심 등록/해제한다.")
  public ResponseEntity<BaseResponse<InterestToggleResponse>> toggle(
      @AuthenticationPrincipal CustomUserDetails ud, @PathVariable Long marketId) {

    var res = userInterestService.toggle(ud.getUser().getId(), marketId);
    return ResponseEntity.ok(BaseResponse.success("OK", res));
  }

  @GetMapping("/markets")
  @Operation(summary = "관심 목록 조회", description = "사용자의 관심 마켓 목록을 조회한다.")
  public ResponseEntity<BaseResponse<InterestedMarketListResponse>> list(
      @AuthenticationPrincipal CustomUserDetails ud) {

    List<InterestedMarketCard> items = userInterestService.list(ud.getUser().getId());
    return ResponseEntity.ok(BaseResponse.success("OK", new InterestedMarketListResponse(items)));
  }

  // (선택) 단건 상태 확인
  @GetMapping("/markets/{marketId}/status")
  @Operation(summary = "관심 여부 확인", description = "해당 마켓이 관심 등록 상태인지 반환한다.")
  public ResponseEntity<BaseResponse<Boolean>> status(
      @AuthenticationPrincipal CustomUserDetails ud, @PathVariable Long marketId) {

    boolean interested = userInterestService.isInterested(ud.getUser().getId(), marketId);
    return ResponseEntity.ok(BaseResponse.success("OK", interested));
  }
}
