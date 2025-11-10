package com.hackathon.tomolow.domain.group.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JoinableGroupListResponseDto {

  private List<JoinableGroupSummary> joinedGroupList;

  private List<JoinableGroupSummary> notJoinedGroupList;

  @Data
  @Builder
  public static class JoinableGroupSummary {

    private Long groupId;

    private String groupName;

    private int memberCount;

    private int currentMemberCount;
  }
}
