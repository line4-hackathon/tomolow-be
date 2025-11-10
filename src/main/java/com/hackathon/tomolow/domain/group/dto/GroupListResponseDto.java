package com.hackathon.tomolow.domain.group.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupListResponseDto {

  private List<GroupSummary> summaryList;

  @Data
  @Builder
  public static class GroupSummary {

    private String groupName;

    private Long dayUntilEnd;

    private Long hourUntilEnd;

    private int ranking;
  }
}
