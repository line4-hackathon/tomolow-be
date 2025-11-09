package com.hackathon.tomolow.domain.market.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.tomolow.domain.market.dto.response.NewsResponseDto;
import com.hackathon.tomolow.domain.market.service.MarketService;
import com.hackathon.tomolow.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Market", description = "마켓별 트레이딩 페이지")
public class MarketController {

  private final MarketService marketService;

  @GetMapping("/market/{marketId}/news")
  @Operation(summary = "최신 뉴스 조회", description = "최신 뉴스 조회를 위한 API")
  public ResponseEntity<BaseResponse<?>> getRecentNews(@PathVariable Long marketId) {
    List<NewsResponseDto> recentNews = marketService.getRecentNews(marketId);
    return ResponseEntity.ok(BaseResponse.success(recentNews));
  }
}
