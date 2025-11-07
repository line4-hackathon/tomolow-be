package com.hackathon.tomolow.domain.portfilio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackathon.tomolow.domain.portfilio.dto.PortfolioPnlMessage;
import com.hackathon.tomolow.domain.ticker.service.PriceQueryService;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.user.repository.UserRepository;
import com.hackathon.tomolow.domain.userMarketHolding.entity.UserMarketHolding;
import com.hackathon.tomolow.domain.userMarketHolding.repository.UserMarketHoldingRepository;
import com.hackathon.tomolow.global.redis.RedisUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioPnlService {

  private final RedisUtil redisUtil; // 인덱스 재작성 때만 사용
  private final PortfolioRedisService pr; // ✅ 키 관리는 이 서비스 것만 사용
  private final UserRepository userRepository;
  private final UserMarketHoldingRepository holdingRepo;
  private final PriceQueryService priceQueryService;
  private final SimpMessagingTemplate messaging;

  /** 온라인 된 직후 호출: 현재 보유 인덱스를 최신화하고, 절대 PnL을 baseline 으로 저장 + 즉시 1회 푸시 */
  @Transactional(readOnly = true)
  public void onOnline(long userId) {
    reindexUserHoldings(userId); // 보유 심볼 인덱스 최신화
    pushPnlAndSeedBaseline(userId); // 절대 PnL 계산 → baseline 저장 + 즉시 푸시
  }

  /** 사용자의 보유 심볼 인덱스를 Redis 세트로 재작성 (체결 이후/온라인 등록 시 호출) */
  @Transactional(readOnly = true)
  public void reindexUserHoldings(long userId) {
    var t = redisUtil.getTemplate();

    // 기존 인덱스 제거
    Set<String> old = t.opsForSet().members("holdings:user:" + userId);
    if (old != null) {
      for (String s : old) {
        // ✅ 실제 운용 키는 holders:{symbol}
        t.opsForSet().remove("holders:" + s, String.valueOf(userId));
      }
      t.delete("holdings:user:" + userId);
    }

    User user = userRepository.findById(userId).orElse(null);
    if (user == null) {
      return;
    }

    List<UserMarketHolding> hs = holdingRepo.findAllByUser(user);
    Set<String> symbols =
        hs.stream()
            .filter(h -> h.getQuantity() != null && h.getQuantity() > 0)
            .map(h -> h.getMarket().getSymbol())
            .collect(Collectors.toCollection(LinkedHashSet::new));

    // ✅ 양방향 인덱스 세팅(표면 키는 유지, 실제 보유자 세트는 holders:{symbol})
    for (String s : symbols) {
      t.opsForSet().add("holdings:user:" + userId, s);
      pr.addHolder(s, userId); // holders:{symbol} 세트
    }
  }

  /** 틱이 특정 심볼로 들어왔을 때, 그 심볼을 보유하고 '온라인'인 유저만 추려서 푸시 */
  @Transactional(readOnly = true)
  public void pushPnlForSymbol(String symbol) {
    // ✅ 실제 보유자 세트 사용
    Set<String> holders = pr.getHolders(symbol);
    if (holders.isEmpty()) {
      return;
    }

    // ✅ 실제 온라인 세트 사용
    Set<String> online = pr.getOnlineUsers();
    if (online.isEmpty()) {
      return;
    }

    for (String uidStr : holders) {
      if (!online.contains(uidStr)) {
        continue;
      }
      long userId = Long.parseLong(uidStr);
      pushPnlAndSeedBaseline(userId); // 절대 PnL 계산해 푸시(기존 baseline은 그대로 두어도 무방)
    }
  }

  /** 단일 사용자에 대해 '절대' 총손익/손익률 계산 → baseline 저장(+즉시 푸시) */
  @Transactional(readOnly = true)
  public void pushPnlAndSeedBaseline(long userId) {
    User user = userRepository.findById(userId).orElse(null);
    if (user == null) {
      return;
    }

    List<UserMarketHolding> holdings = holdingRepo.findAllByUser(user);

    BigDecimal totalPnl = BigDecimal.ZERO;
    BigDecimal invest = user.getInvestmentBalance(); // 매입 원금(고정)

    for (UserMarketHolding h : holdings) {
      if (h.getQuantity() == null || h.getQuantity() <= 0) {
        continue;
      }

      String symbol = h.getMarket().getSymbol();
      BigDecimal cur = priceQueryService.getLastTradePriceOrThrow(symbol);

      BigDecimal qty = BigDecimal.valueOf(h.getQuantity());
      BigDecimal inv = h.getAvgPrice().multiply(qty);
      BigDecimal now = cur.multiply(qty);

      totalPnl = totalPnl.add(now.subtract(inv));
    }

    BigDecimal rate =
        invest.signum() == 0 ? BigDecimal.ZERO : totalPnl.divide(invest, 4, RoundingMode.HALF_UP);

    // ✅ baseline 세팅: 현재 절대 PnL을 pnlKey 에 저장(증분 누적의 시작점)
    pr.resetPnl(userId);
    pr.incrPnl(userId, totalPnl.doubleValue());

    // 즉시 1회 푸시
    messaging.convertAndSend(
        "/topic/portfolio/" + userId,
        PortfolioPnlMessage.builder()
            .totalPnlAmount(totalPnl)
            .totalPnlRate(rate)
            .ts(System.currentTimeMillis())
            .build());
  }
}
