package com.hackathon.tomolow.domain.userGroupTransaction.controller;

import java.math.BigDecimal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.hackathon.tomolow.domain.transaction.dto.OrderRequestDto;
import com.hackathon.tomolow.domain.userGroupTransaction.dto.GroupInfoResponseDto;
import com.hackathon.tomolow.domain.userGroupTransaction.service.GroupOrderInfoService;
import com.hackathon.tomolow.domain.userGroupTransaction.service.LimitGroupOrderService;
import com.hackathon.tomolow.domain.userGroupTransaction.service.MarketGroupOrderService;
import com.hackathon.tomolow.global.response.BaseResponse;
import com.hackathon.tomolow.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/group")
@Tag(name = "Group Buy/Sell", description = "그룹 매수/매도 관련 API")
public class GroupOrderController {

  private final MarketGroupOrderService marketGroupOrderService;
  private final GroupOrderInfoService groupOrderInfoService;
  private final LimitGroupOrderService limitGroupOrderService;

  @PostMapping("/{groupId}/buy/market/{marketId}")
  @Operation(summary = "그룹 시장가 매수", description = "그룹 시장가 매수를 위한 API")
  public ResponseEntity<BaseResponse<?>> groupMarketBuyOrder(
      @PathVariable Long marketId,
      @PathVariable Long groupId,
      @Valid @RequestBody OrderRequestDto orderRequestDto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long userId = userDetails.getUser().getId();
    marketGroupOrderService.marketBuy(userId, groupId, marketId, orderRequestDto);
    return ResponseEntity.ok(BaseResponse.success(null));
  }

  @PostMapping("/{groupId}/sell/market/{marketId}")
  @Operation(summary = "그룹 시장가 매도", description = "그룹 시장가 매도를 위한 API")
  public ResponseEntity<BaseResponse<?>> groupMarketSellOrder(
      @PathVariable Long marketId,
      @PathVariable Long groupId,
      @Valid @RequestBody OrderRequestDto orderRequestDto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long userId = userDetails.getUser().getId();
    marketGroupOrderService.marketSell(userId, groupId, marketId, orderRequestDto);
    return ResponseEntity.ok(BaseResponse.success(null));
  }

  @PostMapping("/{groupId}/buy/limit/{marketId}")
  @Operation(summary = "그룹 지정가 매수", description = "그룹 지정가 매수를 위한 API")
  public ResponseEntity<BaseResponse<?>> groupLimitBuyOrder(
      @PathVariable Long marketId,
      @PathVariable Long groupId,
      @Valid @RequestBody OrderRequestDto orderRequestDto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long userId = userDetails.getUser().getId();
    String buyOrderId = limitGroupOrderService.limitBuy(userId, groupId, marketId, orderRequestDto);
    return ResponseEntity.ok(BaseResponse.success(buyOrderId));
  }

  @PostMapping("/{groupId}/sell/limit/{marketId}")
  @Operation(summary = "그룹 지정가 매도", description = "그룹 지정가 매도를 위한 API")
  public ResponseEntity<BaseResponse<?>> groupLimitSellOrder(
      @PathVariable Long marketId,
      @PathVariable Long groupId,
      @Valid @RequestBody OrderRequestDto orderRequestDto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long userId = userDetails.getUser().getId();
    String sellOrderId =
        limitGroupOrderService.limitSell(userId, groupId, marketId, orderRequestDto);
    return ResponseEntity.ok(BaseResponse.success(sellOrderId));
  }

  @GetMapping("/{groupId}/buy/market/{marketId}")
  @Operation(
      summary = "그룹 시장가 거래 - 시장가 / 최대 매수 수량 / 보유 현금 조회",
      description = "시장가 거래 - 시장가 / 최대 매수 수량 / 보유 현금 조회를 위한 API")
  public ResponseEntity<BaseResponse<?>> getGroupMarketBuyInfo(
      @PathVariable Long marketId,
      @PathVariable Long groupId,
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    Long userId = customUserDetails.getUser().getId();
    GroupInfoResponseDto groupMarketBuyInfo =
        groupOrderInfoService.getGroupMarketBuyInfo(userId, groupId, marketId);
    return ResponseEntity.ok(BaseResponse.success(groupMarketBuyInfo));
  }

  @GetMapping("/{groupId}/buy/limit/{marketId}")
  @Operation(
      summary = "지정가 거래 - 최대 매수 수량 / 보유 현금 조회",
      description = "지정가 거래 - 최대 매수 수량 / 보유 현금 조회를 위한 API")
  public ResponseEntity<BaseResponse<?>> getGroupLimitBuyInfo(
      @PathVariable Long marketId,
      @PathVariable Long groupId,
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @RequestParam @NotNull BigDecimal price) {
    Long userId = customUserDetails.getUser().getId();
    GroupInfoResponseDto groupLimitBuyInfo =
        groupOrderInfoService.getGroupLimitBuyInfo(userId, groupId, marketId, price);
    return ResponseEntity.ok(BaseResponse.success(groupLimitBuyInfo));
  }

  @GetMapping("/{groupId}/sell/{marketId}")
  @Operation(summary = "시장가 및 최대 매도 수량 조회", description = "지정가, 시장가 거래 - 시장가 및 최대 매도 수량 조회를 위한 API")
  public ResponseEntity<BaseResponse<?>> getGroupSellInfo(
      @PathVariable Long marketId,
      @PathVariable Long groupId,
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    Long userId = customUserDetails.getUser().getId();
    GroupInfoResponseDto groupSellInfo =
        groupOrderInfoService.getGroupSellInfo(userId, groupId, marketId);
    return ResponseEntity.ok(BaseResponse.success(groupSellInfo));
  }
}
