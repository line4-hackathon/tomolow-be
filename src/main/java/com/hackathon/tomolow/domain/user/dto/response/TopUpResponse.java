package com.hackathon.tomolow.domain.user.dto.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 💰 머니 충전 결과 응답 DTO - 마이페이지의 자산 그래프 및 현금/투자자산 영역 갱신용 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "TopUpResponse", description = "머니 충전 후 최신 자산 상태 응답 DTO")
public class TopUpResponse {

  @Schema(description = "충전 후 현금 잔액", example = "12500000.00")
  private BigDecimal cashBalance;

  @Schema(description = "현재 투자 자산 잔액", example = "2333354.00")
  private BigDecimal investmentBalance;

  @Schema(description = "전체 자산 (현금 + 투자)", example = "14833354.00")
  private BigDecimal totalAsset;
}
