package com.hackathon.tomolow.domain.userGroupStockHolding.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

import com.hackathon.tomolow.domain.market.entity.Market;
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
    name = "user_group_market_holding",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_usergroup_market",
          columnNames = {"usergroup_id", "market_id"})
    })
public class UserGroupMarketHolding extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 보유 수량 */
  @Column(name = "quantity", nullable = false)
  private int quantity;

  /** 평균 구매 단가 */
  @Column(name = "avg_price", nullable = false, precision = 19, scale = 2)
  private BigDecimal avgPrice;

  /** 그룹 내 유저 */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usergroup_id", nullable = false)
  private UserGroup userGroup;

  /** 주식 종목 */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "market_id", nullable = false)
  private Market market;

  /* ===== 계산/유틸 메서드 ===== */

  /** 총 매입 금액 (평균단가 × 수량) */
  public BigDecimal getTotalInvestment() {
    return avgPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
  }

  /** 추가 매수 시 평균단가 재계산 */
  public void addQuantity(int additionalQuantity, BigDecimal buyPrice) {
    BigDecimal totalCost =
        avgPrice
            .multiply(BigDecimal.valueOf(quantity))
            .add(buyPrice.multiply(BigDecimal.valueOf(additionalQuantity)));
    this.quantity += additionalQuantity;
    this.avgPrice = totalCost.divide(BigDecimal.valueOf(this.quantity), 2, RoundingMode.HALF_UP);
  }

  /** 매도 시 수량 감소 */
  public void subtractQuantity(int sellQuantity) {
    if (sellQuantity > this.quantity) {
      throw new IllegalArgumentException("보유 수량보다 많은 수량을 매도할 수 없습니다.");
    }
    this.quantity -= sellQuantity;
  }
}
