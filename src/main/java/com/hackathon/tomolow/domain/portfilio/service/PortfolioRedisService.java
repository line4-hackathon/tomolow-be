package com.hackathon.tomolow.domain.portfilio.service;

import java.math.BigDecimal;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioRedisService {

  private final StringRedisTemplate redis;

  private String holdersKey(String symbol) {
    return "holders:" + symbol;
  }

  private String posKey(Long userId, String symbol) {
    return "pos:" + userId + ":" + symbol;
  }

  private String pnlKey(Long userId) {
    return "portfolio:pnl:" + userId;
  }

  private String onlineKey() {
    return "ws:online";
  }

  private String prevPriceKey(String symbol) {
    return "prev_price:" + symbol;
  }

  // ===== 보유자 세트 =====
  public void addHolder(String symbol, Long userId) {
    redis.opsForSet().add(holdersKey(symbol), String.valueOf(userId));
  }

  public void removeHolder(String symbol, Long userId) {
    redis.opsForSet().remove(holdersKey(symbol), String.valueOf(userId));
  }

  public Set<String> getHolders(String symbol) {
    var s = redis.opsForSet().members(holdersKey(symbol));
    return (s == null) ? Set.of() : s;
  }

  // ===== 포지션 미러 =====
  public void setPosition(Long userId, String symbol, long qty, BigDecimal avg) {
    var key = posKey(userId, symbol);
    redis.opsForHash().put(key, "qty", String.valueOf(qty));
    redis.opsForHash().put(key, "avg", avg.toPlainString());
  }

  public long getQty(Long userId, String symbol) {
    var v = (String) redis.opsForHash().get(posKey(userId, symbol), "qty");
    return (v == null) ? 0L : Long.parseLong(v);
  }

  public void deletePosition(Long userId, String symbol) {
    redis.delete(posKey(userId, symbol));
  }

  // ===== PnL 누적 =====
  public void incrPnl(Long userId, double delta) {
    redis.opsForValue().increment(pnlKey(userId), delta);
  }

  public double getPnl(Long userId) {
    var v = redis.opsForValue().get(pnlKey(userId));
    return (v == null) ? 0.0 : Double.parseDouble(v);
  }

  public void resetPnl(Long userId) {
    redis.delete(pnlKey(userId));
  }

  // ===== 온라인 유저 =====
  public void online(Long userId) {
    redis.opsForSet().add(onlineKey(), String.valueOf(userId));
  }

  public void offline(Long userId) {
    redis.opsForSet().remove(onlineKey(), String.valueOf(userId));
  }

  public Set<String> getOnlineUsers() {
    var s = redis.opsForSet().members(onlineKey());
    return (s == null) ? Set.of() : s;
  }

  // ===== prev price =====
  public BigDecimal getPrevPrice(String symbol) {
    var v = redis.opsForValue().get(prevPriceKey(symbol));
    return (v == null) ? null : new BigDecimal(v);
  }

  public void setPrevPrice(String symbol, BigDecimal price) {
    redis.opsForValue().set(prevPriceKey(symbol), price.toPlainString());
  }
}
