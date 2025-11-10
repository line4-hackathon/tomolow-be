package com.hackathon.tomolow.domain.group.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class GroupCreateDto {

  @NotNull private String name;

  @NotNull private BigDecimal seedMoney;

  @NotNull private int memberCount;

  @NotNull private int duration;
}
