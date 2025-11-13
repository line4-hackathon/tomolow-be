package com.hackathon.tomolow.domain.candle.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackathon.tomolow.domain.candle.dto.UpbitDayCandle;
import com.hackathon.tomolow.domain.candle.entity.Candle;
import com.hackathon.tomolow.domain.candle.repository.CandleRepository;
import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.market.repository.MarketRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandleIngestService {

  private static final long INTERVAL_1D = 1440L;
  private static final DateTimeFormatter KST = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  private final UpbitRestClient upbit;
  private final CandleRepository candleRepo;
  private final MarketRepository marketRepo;

  @Transactional
  public int upsertDayCandles(Market market, int count) throws Exception {
    List<UpbitDayCandle> rows = upbit.getDayCandles(market.getSymbol(), count);
    int saved = 0;

    for (UpbitDayCandle r : rows) {
      LocalDateTime start = LocalDateTime.parse(r.getCandleDateTimeKst(), KST);

      // 유니크키(market_id, start_time, interval_min)로 중복 방지
      if (candleRepo
          .findByMarketAndStartTimeAndIntervalMin(market, start, INTERVAL_1D)
          .isPresent()) {
        continue;
      }

      // ⚠️ volume 컬럼 선택: 수량으로 저장할지(아래) 금액으로 저장할지 결정
      // - 수량: r.getAccTradeVolume()
      // - 원화금액(권장, 그래프 합산 이해 쉬움): r.getAccTradePrice()
      BigDecimal volume = r.getAccTradeVolume(); // 원화로 하려면 getAccTradePrice()

      Candle entity =
          Candle.builder()
              .market(market)
              .startTime(start)
              .intervalMin(INTERVAL_1D)
              .openPrice(r.getOpeningPrice())
              .highPrice(r.getHighPrice())
              .lowPrice(r.getLowPrice())
              .closePrice(r.getTradePrice())
              .volume(volume)
              .build();

      candleRepo.save(entity);
      saved++;
    }

    log.info(
        "Upsert 1D candles: {} saved={} (symbol={})", market.getName(), saved, market.getSymbol());
    return saved;
  }

  @Transactional
  public int ingestSingleById(Long marketId, int count) throws Exception {
    Market m =
        marketRepo
            .findById(marketId)
            .orElseThrow(() -> new IllegalArgumentException("해당 시장 없음: id=" + marketId));

    return upsertDayCandles(m, count);
  }
}
