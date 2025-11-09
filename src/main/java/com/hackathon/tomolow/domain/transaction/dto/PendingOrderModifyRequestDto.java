package com.hackathon.tomolow.domain.transaction.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PendingOrderModifyRequestDto {

  @NotNull private String orderId;

  @NotNull private BigDecimal price;
}
