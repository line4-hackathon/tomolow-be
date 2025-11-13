package com.hackathon.tomolow.domain.market.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.tomolow.domain.market.dto.request.MarketAnalysisRequestDto;
import com.hackathon.tomolow.domain.market.dto.response.MarketAnalysisResponseDto;
import com.hackathon.tomolow.domain.market.dto.response.MarketPendingOrderResponseDto;
import com.hackathon.tomolow.domain.market.dto.response.NewsResponseDto;
import com.hackathon.tomolow.domain.market.service.MarketAnalysisService;
import com.hackathon.tomolow.domain.market.service.MarketPendingOrderService;
import com.hackathon.tomolow.domain.market.service.MarketService;
import com.hackathon.tomolow.global.response.BaseResponse;
import com.hackathon.tomolow.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Market", description = "마켓별 트레이딩 페이지")
public class MarketController {

  private final MarketService marketService;
  private final MarketPendingOrderService marketPendingOrderService;
  private final MarketAnalysisService marketAnalysisService;

  @GetMapping("/market/{marketId}/news")
  @Operation(summary = "최신 뉴스 조회", description = "최신 뉴스 조회를 위한 API")
  public ResponseEntity<BaseResponse<?>> getRecentNews(@PathVariable Long marketId) {
    List<NewsResponseDto> recentNews = marketService.getRecentNews(marketId);
    return ResponseEntity.ok(BaseResponse.success(recentNews));
  }

  @PostMapping("/market/{marketId}/analysis")
  @Operation(
      summary = "마켓 AI 주가 분석",
      description = "해당 종목의 최근 뉴스들을 기반으로 ①뉴스 요약 ②현재 상승/하락 요인 ③앞으로의 리스크/기회 를 분석합니다.")
  public ResponseEntity<BaseResponse<MarketAnalysisResponseDto>> analyzeMarket(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long marketId,
      @RequestBody MarketAnalysisRequestDto requestDto) {
    Long userId = customUserDetails.getUser().getId();
    MarketAnalysisResponseDto result =
        marketAnalysisService.analyzeMarket(userId, marketId, requestDto);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(summary = "종목 검색", description = "이름 또는 심볼에 검색어가 포함된 종목을 조회합니다.")
  @GetMapping("/search")
  public ResponseEntity<?> search(@RequestParam("query") String query) {
    return ResponseEntity.ok(BaseResponse.success("검색 결과", marketService.searchMarkets(query)));
  }

  @GetMapping("/market/{marketId}/pending")
  @Operation(summary = "마켓별 대기주문 조회", description = "트레이딩 페이지 내 마켓별 대기주문 조회를 위한 API")
  public ResponseEntity<BaseResponse<?>> getMarketPendingOrder(
      @PathVariable Long marketId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    Long userId = customUserDetails.getUser().getId();
    List<MarketPendingOrderResponseDto> marketPendingOrders =
        marketPendingOrderService.getMarketPendingOrders(userId, marketId);
    return ResponseEntity.ok(BaseResponse.success(marketPendingOrders));
  }

  @GetMapping("/market/{marketId}/pending/group/{groupId}")
  @Operation(summary = "그룹 내 마켓별 대기주문 조회", description = "트레이딩 페이지 내 그룹 내 마켓별 대기주문 조회를 위한 API")
  public ResponseEntity<BaseResponse<?>> getMarketUserGroupPendingOrder(
      @PathVariable Long marketId,
      @PathVariable Long groupId,
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    Long userId = customUserDetails.getUser().getId();
    List<MarketPendingOrderResponseDto> marketPendingOrders =
        marketPendingOrderService.getMarketUserGroupPendingOrders(userId, groupId, marketId);
    return ResponseEntity.ok(BaseResponse.success(marketPendingOrders));
  }
}
