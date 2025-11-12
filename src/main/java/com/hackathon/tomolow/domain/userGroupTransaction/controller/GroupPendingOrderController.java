package com.hackathon.tomolow.domain.userGroupTransaction.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.hackathon.tomolow.domain.transaction.dto.PendingOrderDeleteAndInfoRequestDto;
import com.hackathon.tomolow.domain.transaction.dto.PendingOrderModifyRequestDto;
import com.hackathon.tomolow.domain.userGroupTransaction.dto.UserGroupPendingOrderDto;
import com.hackathon.tomolow.domain.userGroupTransaction.service.UserGroupPendingOrderService;
import com.hackathon.tomolow.global.response.BaseResponse;
import com.hackathon.tomolow.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/group")
@Tag(name = "Group Pending Order", description = "그룹 대기주문 관련 API")
public class GroupPendingOrderController {

  private final UserGroupPendingOrderService userGroupPendingOrderService;

  @GetMapping("{groupId}/pending/list")
  @Operation(summary = "그룹 내 나의 대기 주문 카드(주문 단위) - 이미지/이름/심볼/ID/남은수량/지정가")
  public ResponseEntity<BaseResponse<List<UserGroupPendingOrderDto>>> myPendingOrders(
      @PathVariable Long groupId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long userId = userDetails.getUser().getId();
    List<UserGroupPendingOrderDto> userGroupPendingOrders =
        userGroupPendingOrderService.getUserGroupPendingOrders(userId, groupId);
    return ResponseEntity.ok(BaseResponse.success(userGroupPendingOrders));
  }

  @PostMapping("/{groupId}/pending")
  @Operation(summary = "그룹 대기주문 가격 수정을 위한 시장가 조회")
  public ResponseEntity<BaseResponse<?>> getLatestMarketPrice(
      @PathVariable Long groupId, @RequestBody PendingOrderDeleteAndInfoRequestDto requestDto) {
    BigDecimal latestMarketPrice =
        userGroupPendingOrderService.getLatestMarketPrice(requestDto.getOrderId(), groupId);
    return ResponseEntity.ok(BaseResponse.success(latestMarketPrice));
  }

  @PutMapping("/{groupId}/pending")
  @Operation(summary = "그룹 대기주문 가격 수정")
  public ResponseEntity<BaseResponse<?>> modifyPendingOrders(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @Parameter(description = "그룹 ID", required = true) @PathVariable Long groupId,
      @RequestBody PendingOrderModifyRequestDto requestDto) {
    Long userId = customUserDetails.getUser().getId();
    userGroupPendingOrderService.modifyPendingOrder(userId, groupId, requestDto);
    return ResponseEntity.ok(BaseResponse.success(null));
  }

  @DeleteMapping("/{groupId}/pending")
  @Operation(summary = "그룹 대기주문 삭제")
  public ResponseEntity<BaseResponse<?>> deletePendingOrders(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @Parameter(description = "그룹 ID", required = true) @PathVariable Long groupId,
      @RequestBody PendingOrderDeleteAndInfoRequestDto requestDto) {
    Long userId = customUserDetails.getUser().getId();
    userGroupPendingOrderService.deletePendingOrder(userId, groupId, requestDto.getOrderId());
    return ResponseEntity.ok(BaseResponse.success(null));
  }
}
