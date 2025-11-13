package com.hackathon.tomolow.domain.userGroupTransaction.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.tomolow.domain.transaction.dto.TradeHistoryResponse;
import com.hackathon.tomolow.domain.userGroupTransaction.service.GroupTradeHistoryService;
import com.hackathon.tomolow.global.response.BaseResponse;
import com.hackathon.tomolow.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/group")
@Tag(name = "Group Trade History", description = "그룹 내 개인 거래내역 조회 API")
public class GroupTradeHistoryController {

  private final GroupTradeHistoryService groupTradeHistoryService;

  @Operation(
      summary = "그룹 기간별 거래내역 조회",
      description = "특정 그룹에서, 시작일~종료일 동안의 내 매수/매도 내역과 기간 손익 요약을 조회합니다.")
  @GetMapping("/{groupId}/transactions/history")
  public ResponseEntity<BaseResponse<TradeHistoryResponse>> getGroupTradeHistory(
      @PathVariable Long groupId,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

    Long userId = userDetails.getUser().getId();
    TradeHistoryResponse resp =
        groupTradeHistoryService.getHistory(userId, groupId, startDate, endDate);

    return ResponseEntity.ok(BaseResponse.success("그룹 거래내역 조회 성공", resp));
  }

  @Operation(
      summary = "그룹 기본 범위 거래내역 조회",
      description = "해당 그룹에서의 내 첫 거래일 ~ 오늘 날짜까지의 거래내역과 손익을 조회합니다.")
  @GetMapping("/{groupId}/transactions/history/default")
  public ResponseEntity<BaseResponse<TradeHistoryResponse>> getGroupDefaultTradeHistory(
      @PathVariable Long groupId, @AuthenticationPrincipal CustomUserDetails userDetails) {

    Long userId = userDetails.getUser().getId();
    TradeHistoryResponse resp = groupTradeHistoryService.getDefaultHistory(userId, groupId);

    return ResponseEntity.ok(BaseResponse.success("그룹 거래내역(기본 범위) 조회 성공", resp));
  }
}
