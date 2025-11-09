package com.hackathon.tomolow.domain.group.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupSearchResponseDto {

  private String groupName;

  private String creatorName;

  private BigDecimal seedMoney;

  private int memberCount;

  private int currentMemberCount;
}
