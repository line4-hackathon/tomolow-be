package com.hackathon.tomolow.domain.transaction.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.tomolow.domain.transaction.dto.PendingOrderCardDto;
import com.hackathon.tomolow.domain.transaction.service.PendingOrderQueryService;
import com.hackathon.tomolow.global.response.BaseResponse;
import com.hackathon.tomolow.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class PendingOrderController {

  private final PendingOrderQueryService pendingOrderQueryService;

  @GetMapping("/pending")
  @Operation(summary = "내 미체결 주문 카드(주문 단위) - 이미지/이름/심볼/ID/남은수량/지정가")
  public ResponseEntity<BaseResponse<List<PendingOrderCardDto>>> myPendingOrders(
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    Long userId = userDetails.getUser().getId();
    var cards = pendingOrderQueryService.getMyPendingOrders(userId);
    return ResponseEntity.ok(BaseResponse.success("OK", cards));
  }
}
