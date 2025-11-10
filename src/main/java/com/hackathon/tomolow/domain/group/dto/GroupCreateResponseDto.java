package com.hackathon.tomolow.domain.group.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupCreateResponseDto {

  private Long groupId;

  private String groupName;

  private String groupCode;
}
