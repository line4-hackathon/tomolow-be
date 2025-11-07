package com.hackathon.tomolow.domain.portfilio.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.hackathon.tomolow.domain.portfilio.service.PortfolioPnlService;
import com.hackathon.tomolow.domain.portfilio.service.PortfolioRedisService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PortfolioWsController {

  private final PortfolioRedisService portfolioRedisService;
  private final PortfolioPnlService portfolioPnlService; // ✅ 추가

  // 클라이언트: /app/portfolio/online 로 userId 전송
  @MessageMapping("/portfolio/online")
  public void online(@Payload Long userId) {
    if (userId == null) {
      return;
    }
    portfolioRedisService.online(userId);
    portfolioPnlService.onOnline(userId); // ✅ 인덱스 재작성 + baseline 저장 + 즉시 1회 푸시
  }

  @MessageMapping("/portfolio/offline")
  public void offline(@Payload Long userId) {
    if (userId == null) {
      return;
    }
    portfolioRedisService.offline(userId);
  }
}
