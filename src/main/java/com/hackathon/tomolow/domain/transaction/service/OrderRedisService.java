package com.hackathon.tomolow.domain.transaction.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

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

  private String buyKey(String marketId) {
    return "order:book:BUY:" + marketId;
  }

  private String sellKey(String marketId) {
    return "order:book:SELL:" + marketId;
  }

  private String detailKey(String orderId) {
    return "order:detail:" + orderId;
  }

  // 유저별 대기 주문 목록(주문ID Set) 조회용 key
  private String userOpenOrdersKey(String userId) {
    return "user:openOrders:" + userId;
  }

  // 주문이 남아있는 marketId들을 보관하는 세트의 키 이름
  private String pendingSetKey() {
    return "order:pending:markets";
  }

  /** 주문 저장 */
  public void saveOrder(
      String marketId,
      String orderId,
      TradeType tradeType,
      BigDecimal price,
      int quantity,
      String userId) {
    double priceDouble = price.doubleValue();

    // 대기주문을 위해 ZSET 삽입
    if (tradeType == TradeType.BUY) {
      redisTemplate.opsForZSet().add(buyKey(marketId), orderId, priceDouble);
    } else {
      redisTemplate.opsForZSet().add(sellKey(marketId), orderId, priceDouble);
    }

    // 주문 정보 저장
    redisTemplate.opsForHash().put(detailKey(orderId), "userId", userId);
    redisTemplate.opsForHash().put(detailKey(orderId), "marketId", marketId); // ✅ 추가(무해)
    redisTemplate.opsForHash().put(detailKey(orderId), "price", String.valueOf(price));
    redisTemplate.opsForHash().put(detailKey(orderId), "quantity", String.valueOf(quantity));
    redisTemplate.opsForHash().put(detailKey(orderId), "remaining", String.valueOf(quantity));
    redisTemplate.opsForHash().put(detailKey(orderId), "tradeType", tradeType.name());

    // 유저별 대기주문 Set에 등록
    redisTemplate.opsForSet().add(userOpenOrdersKey(userId), orderId);

        // 마켓별 대기주문 세트에 마켓 등록
        redisTemplate.opsForSet().add(pendingSetKey(), marketId);
  }

  /** 최상위 매수/매도 orderId 조회 */
  public String getHighestBuy(String marketId) {
    return redisTemplate.opsForZSet().reverseRange(buyKey(marketId), 0, 0).stream()
        .findFirst()
        .orElse(null);
  }

  public String getLowestSell(String marketId) {
    return redisTemplate.opsForZSet().range(sellKey(marketId), 0, 0).stream()
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
    if (value == null) {
      throw new CustomException(TransactionErrorCode.PRICE_NOT_EXIST);
    } else {
      return new BigDecimal(value);
    }
  }

  public String getUserId(String orderId) {
    return (String) redisTemplate.opsForHash().get(detailKey(orderId), "userId");
  }

  // ✅ 주문 → 마켓ID
  public String getOrderMarketId(String orderId) {
    return (String) redisTemplate.opsForHash().get(detailKey(orderId), "marketId");
  }

  // ✅ (선택) 주문 side
  public TradeType getTradeType(String orderId) {
    String v = (String) redisTemplate.opsForHash().get(detailKey(orderId), "tradeType");
    return (v == null) ? null : TradeType.valueOf(v);
  }

  /** 잔량 업데이트 */
  public void updateOrRemove(String orderId, String marketId, TradeType tradeType, int quantity) {
    String detail = detailKey(orderId);

    int remaining = getRemainingQuantity(orderId) - quantity;

    if (remaining <= 0) {
      removeOrder(marketId, tradeType, orderId);
    } else {
      redisTemplate.opsForHash().put(detail, "remaining", String.valueOf(remaining));
    }
  }

  /** 주문 제거 */
  public void removeOrder(String marketId, TradeType tradeType, String orderId) {
    String key = tradeType == TradeType.BUY ? buyKey(marketId) : sellKey(marketId);

    String detail = detailKey(orderId);

    // order book과 detail에서 삭제
    redisTemplate.opsForZSet().remove(key, orderId);
    redisTemplate.delete(detailKey(orderId));

    // 유저별 대기주문 세트에서 삭제
    String userId = (String) redisTemplate.opsForHash().get(detail, "userId");

    if (userId != null) {
      redisTemplate.opsForSet().remove(userOpenOrdersKey(userId), orderId); // ✅
    }

    // 해당 마켓의 BUY/SELL ZSET이 모두 비었으면 마켓별 대기주문 세트에서 제거
    if (!hasAnyOpenOrdersForMarket(marketId)) {
      redisTemplate.opsForSet().remove(pendingSetKey(), marketId);
    }
  }

  /** 전체 데이터 삭제 */
  public void deleteAllOrders() {
    var keys = redisTemplate.keys("order:*");
    if (keys != null && !keys.isEmpty()) {
      redisTemplate.delete(keys);
    }
  }

  /** 특정 가격 이상의 매수 주문 조회 */
  public List<String> findBuyOrderAtOrAbovePrice(String marketId, BigDecimal marketPrice) {
    var result =
        redisTemplate
            .opsForZSet()
            .reverseRangeByScore(
                buyKey(marketId), marketPrice.doubleValue(), Double.POSITIVE_INFINITY);
    return (result != null) ? result.stream().toList() : List.of();
  }

  /** 특정 가격 이하의 매도 주문 조회 */
  public List<String> findSellOrderAtOrBelowPrice(String marketId, BigDecimal marketPrice) {
    var result =
        redisTemplate
            .opsForZSet()
            .rangeByScore(sellKey(marketId), Double.NEGATIVE_INFINITY, marketPrice.doubleValue());
    return (result != null) ? result.stream().toList() : List.of();
  }

  // ✅ 유저별 미체결 주문 목록(주문ID) 조회
  public Set<String> listUserOpenOrderIds(String userId) {
    Set<String> s = redisTemplate.opsForSet().members(userOpenOrdersKey(userId));
    return (s == null) ? Set.of() : s;
  }

  // ✅ 해당 마켓에 미체결 주문이 하나라도 남아있는지
  private boolean hasAnyOpenOrdersForMarket(String marketId) {
    Long buyCnt = redisTemplate.opsForZSet().zCard(buyKey(marketId));
    Long sellCnt = redisTemplate.opsForZSet().zCard(sellKey(marketId));
    long b = (buyCnt == null) ? 0L : buyCnt;
    long s = (sellCnt == null) ? 0L : sellCnt;
    return (b + s) > 0;
  }

  // 세트에서 모든 marketId를 가져온다
  public Set<String> getPendingMarketIds() {
    Set<String> s = redisTemplate.opsForSet().members(pendingSetKey());
    return (s == null) ? Set.of() : s;
  }

  public String ensureOrderMarketId(String orderId, String fallbackMarketId) {
    Object v = redisTemplate.opsForHash().get(detailKey(orderId), "marketId");
    if (v != null) return v.toString();
    if (fallbackMarketId != null) {
      // 과거 주문 치유(백필)
      redisTemplate.opsForHash().put(detailKey(orderId), "marketId", fallbackMarketId);
      return fallbackMarketId;
    }
    return null;
  }
}
