package com.hackathon.tomolow.domain.ticker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.tomolow.domain.ticker.dto.TickerMessage;
import com.hackathon.tomolow.domain.transaction.exception.TransactionErrorCode;
import com.hackathon.tomolow.global.exception.CustomException;
import com.hackathon.tomolow.global.redis.RedisUtil;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PriceQueryService {

  private final RedisUtil redisUtil;
  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * 최신 체결가(현재가)를 가져온다. 1) last_price:{symbol} (문자열) 우선 2) 없다면 ticker:{symbol} (JSON) 파싱 없으면
   * PRICE_NOT_EXIST
   */
  // 현재가 요청 메서드 !!
  public BigDecimal getLastTradePriceOrThrow(String symbol) {
    // 1) String 값 우선
    String s = redisUtil.getData("last_price:" + symbol);
    if (s != null) {
      try {
        return new BigDecimal(s);
      } catch (NumberFormatException ignored) {
      }
    }

    // 2) JSON fallback
    String json = redisUtil.getData("ticker:" + symbol);
    if (json != null) {
      try {
        TickerMessage t = objectMapper.readValue(json, TickerMessage.class);
        if (t.getTradePrice() != null) {
          return t.getTradePrice();
        }
      } catch (Exception ignored) {
      }
    }

    throw new CustomException(TransactionErrorCode.PRICE_NOT_EXIST, "시세 없음: " + symbol);
  }

  /**
   * (선택) 최대 허용 지연(ms)을 체크하고 싶으면 사용 tradeTimestamp가 maxAgeMs 이내면 가격을 반환, 아니면 예외
   */
  public BigDecimal getFreshTradePriceOrThrow(String symbol, long maxAgeMs) {
    String json = redisUtil.getData("ticker:" + symbol);
    if (json == null) {
      throw new CustomException(TransactionErrorCode.PRICE_NOT_EXIST);
    }

    try {
      TickerMessage t = objectMapper.readValue(json, TickerMessage.class);
      long now = Instant.now().toEpochMilli();
      if (t.getTradeTimestamp() == 0L || now - t.getTradeTimestamp() > maxAgeMs) {
        throw new CustomException(TransactionErrorCode.PRICE_NOT_EXIST, "가격이 오래됨: " + symbol);
      }
      return t.getTradePrice();
    } catch (Exception e) {
      throw new CustomException(TransactionErrorCode.PRICE_NOT_EXIST);
    }
  }
}
