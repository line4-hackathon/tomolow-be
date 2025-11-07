package com.hackathon.tomolow.domain.portfilio.service;

import java.math.BigDecimal;
import java.util.Set;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioIncrementService {

  private final PortfolioRedisService pr;

  /** 틱 수신 시 호출: Δ가격을 계산하고 보유자들에게 ΔP&L을 누적 */
  public void onTick(String symbol, BigDecimal newPrice) {
    // prev 없으면 세팅만(첫 틱)하고 종료
    var prev = pr.getPrevPrice(symbol);
    if (prev == null) {
      pr.setPrevPrice(symbol, newPrice);
      return;
    }

    BigDecimal delta = newPrice.subtract(prev);
    if (delta.signum() == 0) {
      return;
    }

    // 보유자 순회 → ΔP&L = Δ가격 × 수량
    Set<String> holders = pr.getHolders(symbol);
    if (holders.isEmpty()) {
      pr.setPrevPrice(symbol, newPrice); // 갱신
      return;
    }

    double d = delta.doubleValue();
    for (String uidStr : holders) {
      Long uid = Long.valueOf(uidStr);
      long qty = pr.getQty(uid, symbol);
      if (qty <= 0) {
        continue;
      }
      pr.incrPnl(uid, d * qty);
    }

    // prev 업데이트
    pr.setPrevPrice(symbol, newPrice);
  }
}
