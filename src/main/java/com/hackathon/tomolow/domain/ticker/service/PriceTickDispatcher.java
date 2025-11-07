package com.hackathon.tomolow.domain.ticker.service;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hackathon.tomolow.domain.market.repository.MarketRepository;
import com.hackathon.tomolow.domain.transaction.service.MatchService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PriceTickDispatcher {

  private final MatchService matchService;

  private final ConcurrentMap<String, BigDecimal> lastPrice = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Semaphore> running = new ConcurrentHashMap<>();
  private final ExecutorService pool =
      Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors() / 2));

  public void onTick(String symbol, BigDecimal price) {
    lastPrice.put(symbol, price);

    // 심볼당 동시에 하나만 실행
    running.computeIfAbsent(symbol, k -> new Semaphore(1));
    Semaphore sem = running.get(symbol);
    if (!sem.tryAcquire()) {
      return; // 이미 돌고 있으면 스킵(최신값은 lastPrice에 남아있음)
    }

    pool.submit(
        () -> {
          try {
            BigDecimal p = lastPrice.get(symbol);
            if (p != null) {
              // 👇 Redis 오더북 기반 매칭 (시장가 기준)
              matchService.matchByMarketPrice(loadMarketIdBySymbol(symbol), p);
            }
          } finally {
            sem.release();

            // 실행 중에 새 틱이 더 쌓였으면 한 번 더 처리(꼬리 호출)
            BigDecimal after = lastPrice.get(symbol);
            // 간단한 coalesce: 방금 처리값과 다르면 즉시 한 번 더 시도
            // (더 공격적으로 하려면 루프로 바꿀 수 있음)
            if (after != null && !Thread.currentThread().isInterrupted()) {
              if (after.compareTo(lastPrice.get(symbol)) != 0) {
                onTick(symbol, after);
              }
            }
          }
        });
  }

  // 심볼→마켓ID 매핑은 캐시로 유지 (DB 조회 1회)
  private final ConcurrentMap<String, String> symbolToMarketId = new ConcurrentHashMap<>();

  @Autowired private MarketRepository marketRepository;

  private String loadMarketIdBySymbol(String symbol) {
    return symbolToMarketId.computeIfAbsent(
        symbol,
        s ->
            marketRepository
                .findBySymbol(s)
                .map(m -> String.valueOf(m.getId()))
                .orElseThrow()); // 심볼이 DB에 없으면 설계상 예외
  }
}
