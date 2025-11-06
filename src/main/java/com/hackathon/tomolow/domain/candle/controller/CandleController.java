package com.hackathon.tomolow.domain.candle.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.tomolow.domain.candle.dto.request.CandleTf;
import com.hackathon.tomolow.domain.candle.dto.response.CandlePointResponse;
import com.hackathon.tomolow.domain.candle.service.CandleQueryService;
import com.hackathon.tomolow.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/candles")
@Tag(name = "Candles", description = "차트용 캔들 조회 API")
public class CandleController {

  private final CandleQueryService candleQueryService;

  // 예) /api/candles/KRW-BTC?tf=D1&limit=365
  //    /api/candles/KRW-BTC?tf=W1&limit=104
  //    /api/candles/KRW-BTC?tf=M3&limit=40
  @Operation(summary = "캔들 조회", description = "1D/1W/1M/3M 단위로 캔들을 집계하여 반환")
  @GetMapping("/{symbol}")
  public ResponseEntity<BaseResponse<List<CandlePointResponse>>> get(
      @PathVariable String symbol,
      @RequestParam(defaultValue = "D1") String tf,
      @RequestParam(required = false) Integer limit) {
    List<CandlePointResponse> body =
        candleQueryService.getCandles(symbol, CandleTf.from(tf), limit);
    return ResponseEntity.ok(BaseResponse.success("캔들 조회 성공", body));
  }
}
