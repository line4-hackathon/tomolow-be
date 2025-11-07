package com.hackathon.tomolow.domain.portfilio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PortfolioPushScheduler {

  private final PortfolioRedisService portfolioRedisService;
  private final SimpMessagingTemplate broker;
  private final com.hackathon.tomolow.domain.user.repository.UserRepository userRepo;

  public static record PortfolioPushDto(BigDecimal totalPnlAmount, BigDecimal totalPnlRate) {}

  @Scheduled(fixedDelay = 1000L) // 1초마다 코얼레싱 푸시
  public void push() {
    Set<String> online = portfolioRedisService.getOnlineUsers();
    if (online.isEmpty()) {
      return;
    }

    for (String uidStr : online) {
      Long userId = Long.valueOf(uidStr);

      // 누적된 P&L 금액
      double pnl = portfolioRedisService.getPnl(userId);

      // 투자자산(분모)은 DB에서 읽거나, 캐시해도 됨
      var user = userRepo.findById(userId).orElse(null);
      if (user == null) {
        continue;
      }

      BigDecimal totalInvestment = user.getInvestmentBalance();
      BigDecimal pnlAmt = BigDecimal.valueOf(pnl).setScale(2, RoundingMode.HALF_UP);
      BigDecimal pnlRate =
          (totalInvestment.signum() == 0)
              ? BigDecimal.ZERO
              : pnlAmt.divide(totalInvestment, 4, RoundingMode.HALF_UP);

      var dto = new PortfolioPushDto(pnlAmt, pnlRate);

      // /topic/portfolio/{userId} 로 브로드캐스트
      broker.convertAndSend("/topic/portfolio/" + userId, dto);
    }
  }
}
