package com.hackathon.tomolow.domain.transaction.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class OrderRequestDto {

  @NotNull private int quantity;

  @NotNull private BigDecimal price;

}