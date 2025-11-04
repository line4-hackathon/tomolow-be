package com.hackathon.tomolow.domain.dailyStockPrice.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.hackathon.tomolow.domain.stock.entity.Stock;
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
    name = "daily_stock_price",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_stock_date",
          columnNames = {"stock_id", "date"})
    })
public class DailyStockPrice extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 날짜 (거래일) */
  @Column(name = "date", nullable = false)
  private LocalDate date;

  /** 시가 (open) */
  @Column(name = "open_price", nullable = false)
  private BigDecimal openPrice;

  /** 고가 (high) */
  @Column(name = "high_price", nullable = false)
  private BigDecimal highPrice;

  /** 저가 (low) */
  @Column(name = "low_price", nullable = false)
  private BigDecimal lowPrice;

  /** 종가 (close) */
  @Column(name = "close_price", nullable = false)
  private BigDecimal closePrice;

  /** 종목 (Stock FK) */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "stock_id", nullable = false)
  private Stock stock;
}
