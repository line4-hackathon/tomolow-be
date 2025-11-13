package com.hackathon.tomolow.domain.candle.service;

import com.hackathon.tomolow.domain.market.entity.ExchangeType;
import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.market.repository.MarketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CandleScheduler {

  private final MarketRepository marketRepo;
  private final CandleIngestService ingest;

  // 매일 00:05 KST에 최근 5개만 동기화(업서트)
  @Scheduled(cron = "0 50 3 * * *", zone = "Asia/Seoul")
  public void syncDaily() throws Exception {
    for (Market m : marketRepo.findAll()) {
      if (m.getExchangeType() == ExchangeType.UPBIT) {
        ingest.upsertDayCandles(m, 365 * 3);
      }
    }
  }
}
