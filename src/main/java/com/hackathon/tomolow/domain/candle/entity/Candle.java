package com.hackathon.tomolow.domain.candle.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.global.common.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
    name = "candle",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_candle_market_start_interval",
          columnNames = {"market_id", "start_time", "interval_min"})
    },
    indexes = {
      @Index(name = "idx_candle_market_start", columnList = "market_id,start_time"),
      @Index(name = "idx_candle_interval", columnList = "interval_min")
    })
public class Candle extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 종목 (FK) */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "market_id", nullable = false)
  private Market market;

  /** 캔들의 시작 시간 (예: 2025-11-04T02:40:00) */
  @Column(name = "start_time", nullable = false)
  private LocalDateTime startTime;

  /** 저장 주기(분) — 1, 10, 60, 1440(1D), 10080(1W) 등 */
  @Column(name = "interval_min", nullable = false)
  private Long intervalMin;

  /** 시가 */
  @Column(name = "open_price", nullable = false, precision = 19, scale = 8)
  private BigDecimal openPrice;

  /** 고가 */
  @Column(name = "high_price", nullable = false, precision = 19, scale = 8)
  private BigDecimal highPrice;

  /** 저가 */
  @Column(name = "low_price", nullable = false, precision = 19, scale = 8)
  private BigDecimal lowPrice;

  /** 종가 */
  @Column(name = "close_price", nullable = false, precision = 19, scale = 8)
  private BigDecimal closePrice;

  /** 거래량 (원화/수량 단위 선택 가능; 원화 통일하려면 체결가*수량 합으로 적재) */
  @Column(name = "volume", nullable = false, precision = 20, scale = 4)
  private BigDecimal volume;
}
