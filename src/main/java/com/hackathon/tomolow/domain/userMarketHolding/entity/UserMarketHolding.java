package com.hackathon.tomolow.domain.userStockHolding.entity;

import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.userStockHolding.exception.UserStockHoldingErrorCode;
import com.hackathon.tomolow.global.common.BaseTimeEntity;
import com.hackathon.tomolow.global.exception.CustomException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
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
@Table(name = "user_stock_holding")
public class UserStockHolding extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "stock_id", nullable = false)
  private Market stock; // 주식 (Stock FK)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // 사용자 (User FK)

  @Column(name = "quantity", nullable = false)
  private Long quantity; // 보유 수량

  @Column(name = "avg_price", nullable = false, precision = 19, scale = 2)
  private BigDecimal avgPrice; // 평균 구매 단가

  // ==== 계산용 메서드 ==== //

  /**
   * 총 매입 금액 = 평균단가 × 수량 현재 주식에 투자된 총 금액 계산 (avgPrice × quantity)
   */
  public BigDecimal getTotalInvestment() {
    return avgPrice.multiply(BigDecimal.valueOf(quantity));
  }

  /**
   * 보유 수량 증가 (동일 종목을 추가 매수할 때, 평균단가를 가중평균으로 재계산)
   */
  public void addQuantity(int additionalQuantity, BigDecimal newBuyPrice) {
    // 새로운 평균단가를 계산하는 간단한 로직 (가중평균)
    BigDecimal totalCost =
        avgPrice
            .multiply(BigDecimal.valueOf(quantity))
            .add(newBuyPrice.multiply(BigDecimal.valueOf(additionalQuantity)));
    this.quantity += additionalQuantity;
    this.avgPrice =
        totalCost.divide(BigDecimal.valueOf(this.quantity), 2, BigDecimal.ROUND_HALF_UP);
  }

  /**
   * 수량 차감 (매도 시) 매도 시 수량 차감, 예외 처리 포함
   */
  public void subtractQuantity(int sellQuantity) {
    if (sellQuantity > this.quantity) {
      throw new CustomException(UserStockHoldingErrorCode.INSUFFICIENT_QUANTITY);
    }
    this.quantity -= sellQuantity;
  }
}
