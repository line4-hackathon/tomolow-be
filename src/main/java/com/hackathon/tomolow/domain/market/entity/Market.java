package com.hackathon.tomolow.domain.market.entity;

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
@Table(name = "market")
public class Market extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false)
  private String name; // 주식명

  @Column(name = "symbol", nullable = false, unique = true)
  private String symbol; // 주식 코드 (예: BTC-KRW, DOGE-KRW, AAPL, TSLA 등)

  @Enumerated(EnumType.STRING)
  @Column(name = "asset_type", nullable = false)
  private AssetType assetType; // 거래 시장 (예: CRYPTO, STOCK)

  @Enumerated(EnumType.STRING)
  @Column(name = "exchange_type", nullable = false)
  private ExchangeType exchangeType; // 거래소 이름 (예: Upbit, Binance, Nasdaq)

  @Column(name = "img_url")
  private String imgUrl; // 주식 이미지 (nullable)
}
