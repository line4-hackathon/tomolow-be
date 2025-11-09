package com.hackathon.tomolow.domain.transaction.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.hackathon.tomolow.domain.transaction.dto.PendingOrderCardDto;
import com.hackathon.tomolow.domain.transaction.dto.PendingOrderDeleteAndInfoRequestDto;
import com.hackathon.tomolow.domain.transaction.dto.PendingOrderModifyRequestDto;
import com.hackathon.tomolow.domain.transaction.service.PendingOrderQueryService;
import com.hackathon.tomolow.domain.transaction.service.PendingOrderService;
import com.hackathon.tomolow.global.response.BaseResponse;
import com.hackathon.tomolow.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
@Tag(name = "PendingOrder", description = "대기주문 관련 API" + "")
public class PendingOrderController {

  private final PendingOrderQueryService pendingOrderQueryService;
  private final PendingOrderService pendingOrderService;

  @GetMapping("/pending/list")
  @Operation(summary = "내 미체결 주문 카드(주문 단위) - 이미지/이름/심볼/ID/남은수량/지정가")
  public ResponseEntity<BaseResponse<List<PendingOrderCardDto>>> myPendingOrders(
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    Long userId = userDetails.getUser().getId();
    var cards = pendingOrderQueryService.getMyPendingOrders(userId);
    return ResponseEntity.ok(BaseResponse.success("OK", cards));
  }

  @PostMapping("/pending")
  @Operation(summary = "대기주문 가격 수정을 위한 시장가 조회")
  public ResponseEntity<BaseResponse<?>> getLatestMarketPrice(
      @RequestBody PendingOrderDeleteAndInfoRequestDto requestDto) {
    BigDecimal latestMarketPrice =
        pendingOrderService.getLatestMarketPrice(requestDto.getOrderId());
    return ResponseEntity.ok(BaseResponse.success(latestMarketPrice));
  }

  @PutMapping("/pending")
  @Operation(summary = "대기주문 가격 수정")
  public ResponseEntity<BaseResponse<?>> modifyPendingOrders(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @RequestBody PendingOrderModifyRequestDto requestDto) {
    Long userId = customUserDetails.getUser().getId();
    pendingOrderService.modifyPendingOrder(userId, requestDto);
    return ResponseEntity.ok(BaseResponse.success(null));
  }

  @DeleteMapping("/pending")
  @Operation(summary = "대기주문 삭제")
  public ResponseEntity<BaseResponse<?>> deletePendingOrders(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @RequestBody PendingOrderDeleteAndInfoRequestDto requestDto) {
    Long userId = customUserDetails.getUser().getId();
    pendingOrderService.deletePendingOrder(userId, requestDto.getOrderId());
    return ResponseEntity.ok(BaseResponse.success(null));
  }
}
