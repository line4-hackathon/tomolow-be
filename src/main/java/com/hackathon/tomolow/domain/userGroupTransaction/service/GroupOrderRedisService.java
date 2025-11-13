package com.hackathon.tomolow.domain.userGroupTransaction.service;

import java.math.BigDecimal;
import java.util.HashSet;
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
public class GroupOrderRedisService {

  private final RedisTemplate<String, String> redisTemplate;

  private String groupBuyKey(String marketId, String groupId) {
    return "group:" + groupId + ":order:book:BUY:" + marketId;
  }

  private String groupSellKey(String marketId, String groupId) {
    return "group:" + groupId + ":order:book:SELL:" + marketId;
  }

  private String groupDetailKey(String orderId, String groupId) {
    return "group:" + groupId + ":order:detail:" + orderId;
  }

  // 유저 + 그룹별 대기 주문 목록(주문ID Set) 조회용 key
  private String userGroupOpenOrdersKey(String userId, String groupId) {
    return "group:" + groupId + ":user:openOrders:" + userId;
  }

  // 주문이 남아있는 marketId들을 보관하는 세트의 키 이름
  private String pendingSetKey() {
    return "group:order:pending:markets";
  }

  /** 주문 저장 */
  public void saveOrder(
      String marketId,
      String orderId,
      TradeType tradeType,
      BigDecimal price,
      int quantity,
      String userId,
      String groupId) {
    double priceDouble = price.doubleValue();

    // 대기주문을 위해 ZSET 삽입
    if (tradeType == TradeType.BUY) {
      redisTemplate.opsForZSet().add(groupBuyKey(marketId, groupId), orderId, priceDouble);
    } else {
      redisTemplate.opsForZSet().add(groupSellKey(marketId, groupId), orderId, priceDouble);
    }

    // 주문 정보 저장
    redisTemplate.opsForHash().put(groupDetailKey(orderId, groupId), "userId", userId);
    redisTemplate.opsForHash().put(groupDetailKey(orderId, groupId), "marketId", marketId);
    redisTemplate.opsForHash().put(groupDetailKey(orderId, groupId), "groupId", groupId);
    redisTemplate
        .opsForHash()
        .put(groupDetailKey(orderId, groupId), "price", String.valueOf(price));
    redisTemplate
        .opsForHash()
        .put(groupDetailKey(orderId, groupId), "quantity", String.valueOf(quantity));
    redisTemplate
        .opsForHash()
        .put(groupDetailKey(orderId, groupId), "remaining", String.valueOf(quantity));
    redisTemplate.opsForHash().put(groupDetailKey(orderId, groupId), "tradeType", tradeType.name());

    // 유저별 대기주문 Set에 등록
    redisTemplate.opsForSet().add(userGroupOpenOrdersKey(userId, groupId), orderId);

    // 마켓별 대기주문 세트에 마켓 등록
    redisTemplate.opsForSet().add(pendingSetKey(), marketId);
  }

  /** 주문 상세 조회 */
  public int getRemainingQuantity(String orderId, String groupId) {
    String value =
        (String) redisTemplate.opsForHash().get(groupDetailKey(orderId, groupId), "remaining");
    return (value != null) ? Integer.parseInt(value) : 0;
  }

  public BigDecimal getPrice(String orderId, String groupId) {
    String value =
        (String) redisTemplate.opsForHash().get(groupDetailKey(orderId, groupId), "price");
    if (value == null) {
      throw new CustomException(TransactionErrorCode.PRICE_NOT_EXIST);
    } else {
      return new BigDecimal(value);
    }
  }

  public String getUserId(String orderId, String groupId) {
    return (String) redisTemplate.opsForHash().get(groupDetailKey(orderId, groupId), "userId");
  }

  public String getOrderMarketId(String orderId, String groupId) {
    return (String) redisTemplate.opsForHash().get(groupDetailKey(orderId, groupId), "marketId");
  }

  public TradeType getTradeType(String orderId, String groupId) {
    String v =
        (String) redisTemplate.opsForHash().get(groupDetailKey(orderId, groupId), "tradeType");
    return (v == null) ? null : TradeType.valueOf(v);
  }

  /** 잔량 업데이트 */
  public void updateOrRemove(
      String orderId, String marketId, TradeType tradeType, int quantity, String groupId) {
    String detail = groupDetailKey(orderId, groupId);

    int remaining = getRemainingQuantity(orderId, groupId) - quantity;

    if (remaining <= 0) {
      removeOrder(marketId, tradeType, orderId, groupId);
    } else {
      redisTemplate.opsForHash().put(detail, "remaining", String.valueOf(remaining));
    }
  }

  /** 주문 제거 */
  public void removeOrder(String marketId, TradeType tradeType, String orderId, String groupId) {
    String key =
        tradeType == TradeType.BUY
            ? groupBuyKey(marketId, groupId)
            : groupSellKey(marketId, groupId);

    String detail = groupDetailKey(orderId, groupId);

    String userId = (String) redisTemplate.opsForHash().get(detail, "userId");

    // order book과 detail에서 삭제
    redisTemplate.opsForZSet().remove(key, orderId);
    redisTemplate.delete(groupDetailKey(orderId, groupId));

    if (userId != null) {
      // UserGroup별 대기주문 세트에서 삭제
      redisTemplate.opsForSet().remove(userGroupOpenOrdersKey(userId, groupId), orderId);
    }

    // 해당 마켓의 BUY/SELL ZSET이 모두 비었으면 마켓별 대기주문 세트에서 제거
    if (!hasAnyOpenOrdersForMarket(marketId, groupId)) {
      redisTemplate.opsForSet().remove(pendingSetKey(), marketId);
    }
  }

  /** 전체 데이터 삭제 */
  public void deleteAllOrders() {
    var keys = redisTemplate.keys("group:*");
    if (keys != null && !keys.isEmpty()) {
      redisTemplate.delete(keys);
    }
  }

  /** 특정 가격 이상의 매수 주문 조회 -> orderId 반환 */
  public List<String> findBuyOrderAtOrAbovePrice(
      String marketId, BigDecimal marketPrice, String groupId) {
    var result =
        redisTemplate
            .opsForZSet()
            .reverseRangeByScore(
                groupBuyKey(marketId, groupId),
                marketPrice.doubleValue(),
                Double.POSITIVE_INFINITY);
    return (result != null) ? result.stream().toList() : List.of();
  }

  /** 특정 가격 이하의 매도 주문 조회 -> orderId 반환 */
  public List<String> findSellOrderAtOrBelowPrice(
      String marketId, BigDecimal marketPrice, String groupId) {
    var result =
        redisTemplate
            .opsForZSet()
            .rangeByScore(
                groupSellKey(marketId, groupId),
                Double.NEGATIVE_INFINITY,
                marketPrice.doubleValue());
    return (result != null) ? result.stream().toList() : List.of();
  }

  /** marketId를 기준으로 열린 groupId 조회 */
  public Set<String> getGroupsWithOpenOrders(String marketId) {
    // group:*:order:book:*:marketId 에 해당하는 키들을 전부 가져오기
    Set<String> keys = redisTemplate.keys("group:*:order:book:*:" + marketId);

    if (keys == null || keys.isEmpty()) {
      return Set.of();
    }

    return keys.stream().map(key -> key.split(":")[1]).collect(java.util.stream.Collectors.toSet());
  }

  /** UserGroup별 미체결 주문 목록(주문ID) 조회 */
  public Set<String> listUserGroupOpenOrderIds(String userId, String groupId) {
    Set<String> s = redisTemplate.opsForSet().members(userGroupOpenOrdersKey(userId, groupId));
    return (s == null) ? Set.of() : s;
  }

  /** 해당 마켓에 미체결 주문이 하나라도 남아있는지 */
  private boolean hasAnyOpenOrdersForMarket(String marketId, String groupId) {
    Long buyCnt = redisTemplate.opsForZSet().zCard(groupBuyKey(marketId, groupId));
    Long sellCnt = redisTemplate.opsForZSet().zCard(groupSellKey(marketId, groupId));
    long b = (buyCnt == null) ? 0L : buyCnt;
    long s = (sellCnt == null) ? 0L : sellCnt;
    return (b + s) > 0;
  }

  /** 세트에서 모든 marketId를 가져온다 -> 주기적으로 마켓별로 매칭하기 위함 */
  public Set<String> getPendingMarketIds() {
    Set<String> s = redisTemplate.opsForSet().members(pendingSetKey());
    return (s == null) ? Set.of() : s;
  }

  public String ensureOrderMarketId(String orderId, String fallbackMarketId, String groupId) {
    Object v = redisTemplate.opsForHash().get(groupDetailKey(orderId, groupId), "marketId");
    if (v != null) return v.toString();
    if (fallbackMarketId != null) {
      // 과거 주문 치유(백필)
      redisTemplate
          .opsForHash()
          .put(groupDetailKey(orderId, groupId), "marketId", fallbackMarketId);
      return fallbackMarketId;
    }
    return null;
  }

  /** order:detail의 price 수정 */
  public void updatePrice(String orderId, BigDecimal price, String groupId) {
    String detail = groupDetailKey(orderId, groupId);
    redisTemplate
        .opsForHash()
        .put(groupDetailKey(orderId, groupId), "price", String.valueOf(price));
  }

  /** order book 업데이트 (삭제 및 재생성) */
  public void updateOrderBook(
      String orderId, String marketId, TradeType tradeType, BigDecimal price, String groupId) {
    double priceDouble = price.doubleValue();
    String key =
        tradeType == TradeType.BUY
            ? groupBuyKey(marketId, groupId)
            : groupSellKey(marketId, groupId);

    // 삭제
    redisTemplate.opsForZSet().remove(key, orderId);

    // 재생성
    redisTemplate.opsForZSet().add(key, orderId, priceDouble);
  }

  /** 그룹, 유저, 마켓에 해당하는 주문 조회 */
  public Set<String> getOrdersByGroupAndMarketAndUser(
      String userId, String groupId, String marketId) {
    // 그룹 내 유저에 해당하는 모든 주문 가져오기
    Set<String> userGroupOrders =
        redisTemplate.opsForSet().members(userGroupOpenOrdersKey(userId, groupId));
    if (userGroupOrders == null || userGroupOrders.isEmpty()) return Set.of();

    // 마켓에 해당하는 주문만 필터링
    Set<String> result = new HashSet<>();
    for (String orderId : userGroupOrders) {
      String orderMarketId = getOrderMarketId(orderId, groupId);
      if (orderMarketId != null && orderMarketId.equals(marketId)) result.add(orderId);
    }
    return result;
  }
}
