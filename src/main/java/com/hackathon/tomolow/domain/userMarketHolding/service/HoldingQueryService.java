package com.hackathon.tomolow.domain.userMarketHolding.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.ticker.service.PriceQueryService;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.userMarketHolding.dto.HoldingItemResponse;
import com.hackathon.tomolow.domain.userMarketHolding.dto.HoldingsResponse;
import com.hackathon.tomolow.domain.userMarketHolding.dto.PortfolioSummaryResponse;
import com.hackathon.tomolow.domain.userMarketHolding.entity.UserMarketHolding;
import com.hackathon.tomolow.domain.userMarketHolding.repository.UserMarketHoldingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HoldingQueryService {

  private final UserMarketHoldingRepository holdingRepo;
  private final PriceQueryService priceQueryService;

  @Transactional(readOnly = true)
  public HoldingsResponse getMyHoldings(User user) {
    List<UserMarketHolding> holdings = holdingRepo.findAllByUser(user);

    BigDecimal totalPnl = BigDecimal.ZERO;
    BigDecimal totalInvestment = user.getInvestmentBalance(); // 고정값
    BigDecimal cashBalance = user.getCashBalance(); // 고정값

    for (UserMarketHolding h : holdings) {
      BigDecimal cur = priceQueryService.getLastTradePriceOrThrow(h.getMarket().getSymbol());
      BigDecimal qty = BigDecimal.valueOf(h.getQuantity());

      BigDecimal invest = h.getAvgPrice().multiply(qty);
      BigDecimal current = cur.multiply(qty);

      BigDecimal pnl = current.subtract(invest); // 손익
      totalPnl = totalPnl.add(pnl);
    }

    BigDecimal pnlRate =
        totalInvestment.signum() == 0
            ? BigDecimal.ZERO
            : totalPnl.divide(totalInvestment, 4, RoundingMode.HALF_UP);

    PortfolioSummaryResponse portfolio =
        PortfolioSummaryResponse.builder()
            .totalInvestment(totalInvestment) // ✅ 투자자산 (고정)
            .cashBalance(cashBalance) // ✅ 현금자산 (고정)
            .totalPnlAmount(totalPnl) // ✅ 투자손익원
            .totalPnlRate(pnlRate) // ✅ 투자손익률
            .totalCurrentValue(totalInvestment.add(cashBalance)) // ✅ 전체자산
            .build();

    return HoldingsResponse.builder()
        .items(holdings.stream().map(this::toItem).toList()) // ✅ Stream으로 전체 변환
        .portfolio(portfolio)
        .build();
  }

  private HoldingItemResponse toItem(UserMarketHolding h) {
    Market m = h.getMarket();

    // 🔹 실시간 현재가(레디스) — 가격 없으면 예외
    BigDecimal cur = priceQueryService.getLastTradePriceOrThrow(m.getSymbol());

    BigDecimal qty = BigDecimal.valueOf(h.getQuantity());
    BigDecimal invest = h.getAvgPrice().multiply(qty);
    BigDecimal current = cur.multiply(qty);
    BigDecimal pnl = current.subtract(invest);

    BigDecimal pnlRate =
        (invest.signum() == 0) ? BigDecimal.ZERO : pnl.divide(invest, 4, RoundingMode.HALF_UP);

    return HoldingItemResponse.builder()
        .marketId(m.getId())
        .symbol(m.getSymbol())
        .name(m.getName())
        .imageUrl(m.getImgUrl()) // 컬럼/필드가 imgUrl 이라고 가정 (없으면 null)
        .quantity(h.getQuantity())
        .avgPrice(h.getAvgPrice())
        .currentPrice(cur)
        .pnlAmount(pnl.setScale(2, RoundingMode.HALF_UP))
        .pnlRate(pnlRate)
        .build();
  }
}
