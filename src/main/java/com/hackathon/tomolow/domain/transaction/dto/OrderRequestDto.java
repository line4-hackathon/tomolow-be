package com.hackathon.tomolow.domain.transaction.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "OrderRequest DTO", description = "지정가 주문 요청 본문")
public class OrderRequestDto {

  @NotNull
  @Schema(description = "주문 수량(정수)", example = "2", minimum = "1")
  private int quantity;

  @NotNull
  @Schema(description = "지정가(가격)", example = "3000", minimum = "0")
  private BigDecimal price;
}
