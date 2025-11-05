package com.hackathon.tomolow.domain.stock.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
@Table(name = "stock")
public class Stock extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false)
  private String name; // 주식명

  @Column(name = "code", nullable = false, unique = true)
  private String code; // 주식 코드 (예: AAPL, TSLA 등)

  @Enumerated(EnumType.STRING)
  @Column(name = "market_type", nullable = false)
  private MarketType marketType; // 거래 시장 (예: CRYPTO, KOSPI, NASDAQ, NYSE 등)

  @Enumerated(EnumType.STRING)
  @Column(name = "exchange_type", nullable = false)
  private ExchangeType exchangeType; // 거래소 이름 (예: Upbit, Binance, Coinbase)

  @Column(name = "img_url")
  private String imgUrl; // 주식 이미지 (nullable)
}
