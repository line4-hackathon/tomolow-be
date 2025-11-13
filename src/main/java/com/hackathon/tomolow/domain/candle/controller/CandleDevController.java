package com.hackathon.tomolow.domain.candle.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.tomolow.domain.candle.service.CandleIngestService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dev")
@Tag(name = "Candles", description = "[개발자] 캔들 데이터 저장용 API")
public class CandleDevController {

  private final CandleIngestService candleIngestService;

  @PostMapping("/candle/ingest")
  public String ingestByMarketId(
      @RequestParam Long marketId, @RequestParam(defaultValue = "200") int count) {

    try {
      int saved = candleIngestService.ingestSingleById(marketId, count);
      return "OK - marketId=" + marketId + " 저장 개수: " + saved;
    } catch (Exception e) {
      log.error("❌ ingestByMarketId 실패: id={}, msg={}", marketId, e.getMessage());
      return "ERROR: " + e.getMessage();
    }
  }
}
