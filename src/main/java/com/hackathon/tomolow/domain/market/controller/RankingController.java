package com.hackathon.tomolow.domain.market.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.tomolow.domain.market.service.RankingService;
import com.hackathon.tomolow.global.response.BaseResponse;
import com.hackathon.tomolow.global.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rank")
public class RankingController {

  private final RankingService rankingService;

  @GetMapping("/{type}") // type: turnover|volume|gainers|losers
  public ResponseEntity<?> top(
      @PathVariable String type,
      @RequestParam(defaultValue = "50") int limit,
      @AuthenticationPrincipal CustomUserDetails user) {
    Long userId = (user == null) ? null : user.getUser().getId();
    return ResponseEntity.ok(
        BaseResponse.success(
            "랭킹 조회 성공", rankingService.getTopWithInterest(type, Math.min(limit, 100), userId)));
  }
}
