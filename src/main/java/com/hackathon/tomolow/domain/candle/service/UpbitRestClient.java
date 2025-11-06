package com.hackathon.tomolow.domain.candle.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.tomolow.domain.candle.dto.UpbitDayCandle;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/** ✅ Upbit REST API Client - 일봉(1D) 데이터 수집 - KRW-BTC, KRW-ETH 등 다양한 마켓 코드 지원 */
@Slf4j
@Service
@RequiredArgsConstructor
@Tag(name = "Upbit REST Client", description = "업비트 일봉(1D) 데이터 조회용 클라이언트")
public class UpbitRestClient {

  private static final String BASE_URL = "https://api.upbit.com/v1/candles/days";
  private final OkHttpClient httpClient = new OkHttpClient();
  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * 📈 업비트 일봉 데이터 조회
   *
   * @param market 마켓 코드 (예: "KRW-BTC", "KRW-ETH")
   * @param count 조회할 일수 (1~200)
   * @return UpbitDayCandle 리스트
   */
  @Operation(
      summary = "업비트 일봉 데이터 조회",
      description = "Upbit REST API를 호출하여 특정 마켓의 일봉(1D) 데이터를 반환합니다.")
  public List<UpbitDayCandle> getDayCandles(String market, int count) throws Exception {
    String url = String.format("%s?market=%s&count=%d", BASE_URL, market, count);
    log.info("Requesting Upbit Daily Candles → {}", url);

    Request request = new Request.Builder().url(url).get().build();

    try (Response response = httpClient.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        throw new RuntimeException("Upbit API 요청 실패: " + response.code());
      }

      String body = response.body().string();
      List<UpbitDayCandle> candles =
          objectMapper.readValue(body, new TypeReference<List<UpbitDayCandle>>() {});

      log.info("✅ Upbit 일봉 데이터 수신 완료 ({}개) market={}", candles.size(), market);
      return candles;
    } catch (Exception e) {
      log.error("❌ Upbit API 요청 실패 (market={}): {}", market, e.getMessage());
      throw e;
    }
  }
}
