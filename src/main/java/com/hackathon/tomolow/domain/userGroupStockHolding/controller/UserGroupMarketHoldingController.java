package com.hackathon.tomolow.domain.userGroupStockHolding.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.hackathon.tomolow.domain.userGroupStockHolding.dto.UserGroupMarketHoldingPnLDto;
import com.hackathon.tomolow.domain.userGroupStockHolding.service.UserGroupMarketHoldingService;
import com.hackathon.tomolow.global.response.BaseResponse;
import com.hackathon.tomolow.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/group")
@Tag(name = "UserGroup Market Holding", description = "그룹 내 보유 종목 조회 API")
public class UserGroupMarketHoldingController {

  private final UserGroupMarketHoldingService userGroupMarketHoldingService;

  @GetMapping("/{groupId}/holding")
  public ResponseEntity<BaseResponse<?>> getUserGroupMarketHolding(
      @PathVariable Long groupId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    Long userId = customUserDetails.getUser().getId();
    UserGroupMarketHoldingPnLDto userGroupMarketHoldings =
        userGroupMarketHoldingService.getUserGroupMarketHoldings(userId, groupId);
    return ResponseEntity.ok(BaseResponse.success(userGroupMarketHoldings));
  }
}
