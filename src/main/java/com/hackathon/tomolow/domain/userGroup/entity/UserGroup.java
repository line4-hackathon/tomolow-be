package com.hackathon.tomolow.domain.userGroup.entity;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.hackathon.tomolow.domain.group.entity.Group;
import com.hackathon.tomolow.domain.user.entity.User;
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
    name = "user_group",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_user_group",
          columnNames = {"user_id", "group_id"})
    })
public class UserGroup extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 그룹 */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id", nullable = false)
  private Group group;

  /** 유저 */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  /** 그룹 내 개인 투자자산 */
  @Column(name = "investment_balance", nullable = false, precision = 19, scale = 2)
  private BigDecimal investmentBalance;

  /** 그룹 내 개인 현금 자산 */
  @Column(name = "cash_balance", nullable = false, precision = 19, scale = 2)
  private BigDecimal cashBalance;

  /* ===== 편의 메서드 ===== */

  @PrePersist
  private void initBalances() {
    if (investmentBalance == null) {
      investmentBalance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
    if (cashBalance == null) {
      cashBalance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
  }

  /** 현금 증감 */
  public void addCash(BigDecimal amount) {
    cashBalance = cashBalance.add(amount).setScale(2, RoundingMode.HALF_UP);
  }

  public void subtractCash(BigDecimal amount) {
    cashBalance = cashBalance.subtract(amount).setScale(2, RoundingMode.HALF_UP);
  }

  /** 투자 잔액 증감 */
  public void addInvestment(BigDecimal amount) {
    investmentBalance = investmentBalance.add(amount).setScale(2, RoundingMode.HALF_UP);
  }

  public void subtractInvestment(BigDecimal amount) {
    investmentBalance = investmentBalance.subtract(amount).setScale(2, RoundingMode.HALF_UP);
  }
}
