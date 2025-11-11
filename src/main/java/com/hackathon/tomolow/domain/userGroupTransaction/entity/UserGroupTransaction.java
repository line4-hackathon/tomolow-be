package com.hackathon.tomolow.domain.userGroupTransaction.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.transaction.entity.TradeType;
import com.hackathon.tomolow.domain.userGroup.entity.UserGroup;
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
    name = "user_group_transaction",
    indexes = {
      @Index(name = "idx_ugt_usergroup", columnList = "usergroup_id"),
      @Index(name = "idx_ugt_market", columnList = "market_id"),
      @Index(name = "idx_ugt_created_at", columnList = "created_at")
    })
public class UserGroupTransaction extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 거래 수량(주/코인 단위) */
  @Column(name = "quantity", nullable = false)
  private int quantity;

  /** 거래 가격 */
  @Column(name = "price", nullable = false)
  private BigDecimal price;

  /** 매수/매도 구분 */
  @Enumerated(EnumType.STRING)
  @Column(name = "trade_type", nullable = false)
  private TradeType tradeType; // BUY, SELL

  /** 그룹 내 개인(유저) */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usergroup_id", nullable = false)
  private UserGroup userGroup;

  /** 거래한 종목 */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "market_id", nullable = false)
  private Market market;

  /* ===== 편의 메서드 ===== */
  @Transient
  public boolean isBuy() {
    return tradeType == TradeType.BUY;
  }

  @Transient
  public boolean isSell() {
    return tradeType == TradeType.SELL;
  }
}
