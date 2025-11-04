package com.hackathon.tomolow.domain.transaction.service;

import java.math.BigDecimal;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.hackathon.tomolow.domain.transaction.entity.TradeType;
import com.hackathon.tomolow.domain.transaction.exception.TransactionErrorCode;
import com.hackathon.tomolow.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderRedisService {

  private final RedisTemplate<String, String> redisTemplate;

  private String buyKey(String stockId) {
    return "order:book:BUY:" + stockId;
  }

  private String sellKey(String stockId) {
    return "order:book:SELL:" + stockId;
  }

  private String detailKey(String orderId) {
    return "order:detail:" + orderId;
  }

  /** 주문 저장 */
  public void saveOrder(
      String stockId,
      String orderId,
      TradeType tradeType,
      BigDecimal price,
      int quantity,
      String userId) {
    double priceDouble = price.doubleValue();

    // 대기주문을 위해 ZSET 삽입
    if (tradeType == TradeType.BUY)
      redisTemplate.opsForZSet().add(buyKey(stockId), orderId, priceDouble);
    else redisTemplate.opsForZSet().add(sellKey(stockId), orderId, priceDouble);

    // 주문 정보 저장
    redisTemplate.opsForHash().put(detailKey(orderId), "userId", userId);
    redisTemplate.opsForHash().put(detailKey(orderId), "price", String.valueOf(price));
    redisTemplate.opsForHash().put(detailKey(orderId), "quantity", String.valueOf(quantity));
    redisTemplate.opsForHash().put(detailKey(orderId), "remaining", String.valueOf(quantity));
    redisTemplate.opsForHash().put(detailKey(orderId), "tradeType", tradeType.name());
  }

  /** 최상위 매수/매도 orderId 조회 */
  public String getHighestBuy(String stockId) {
    return redisTemplate.opsForZSet().reverseRange(buyKey(stockId), 0, 0).stream()
        .findFirst()
        .orElse(null);
  }

  public String getLowestSell(String stockId) {
    return redisTemplate.opsForZSet().range(sellKey(stockId), 0, 0).stream()
        .findFirst()
        .orElse(null);
  }

  /** 주문 상세 조회 */
  public int getRemainingQuantity(String orderId) {
    String value = (String) redisTemplate.opsForHash().get(detailKey(orderId), "remaining");
    return (value != null) ? Integer.parseInt(value) : 0;
  }

  public BigDecimal getPrice(String orderId) {
    String value = (String) redisTemplate.opsForHash().get(detailKey(orderId), "price");
    if (value == null) throw new CustomException(TransactionErrorCode.PRICE_NOT_EXIST);
    else return new BigDecimal(value);
  }

  public String getUserId(String orderId) {
    return (String) redisTemplate.opsForHash().get(detailKey(orderId), "userId");
  }

  /** 잔량 업데이트 */
  public void updateRemaining(String orderId, int remaining) {
    redisTemplate.opsForHash().put(detailKey(orderId), "remaining", String.valueOf(remaining));
  }

  /** 주문 제거 */
  public void removeOrder(String stockId, TradeType tradeType, String orderId) {
    String key = tradeType == TradeType.BUY ? buyKey(stockId) : sellKey(stockId);
    redisTemplate.opsForZSet().remove(key, orderId);
    redisTemplate.delete(detailKey(orderId));
  }

  /** 전체 데이터 삭제 */
  public void deleteAllOrders() {
    var keys = redisTemplate.keys("order:*");
    if (keys != null && !keys.isEmpty()) redisTemplate.delete(keys);
  }
}
