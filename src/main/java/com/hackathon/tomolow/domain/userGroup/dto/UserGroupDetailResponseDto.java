package com.hackathon.tomolow.domain.userGroup.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserGroupDetailResponseDto {

  private Long groupId;

  private String groupName;

  private BigDecimal seedMoney;

  private String code;

  private int memberCount;

  private long currentMemberCount;

  /** 종료까지 남은 시간 */
  private Long days;

  private Long hours;

  private Long minutes;

  /** 자산 현황 */
  private BigDecimal totalBalance;

  private BigDecimal cashBalance;

  private BigDecimal investmentBalance;

  private BigDecimal pnL;

  private BigDecimal pnLRate;

  /** 투자 순위 */
  private List<UserGroupRankingDto> rankings;
}
