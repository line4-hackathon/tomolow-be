package com.hackathon.tomolow.global.s3.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@Schema(title = "S3Response DTO", description = "이미지 업로드에 대한 응답 반환")
public class S3Response {

  @Schema(description = "문제 이미지 URL", example = "https://~")
  private String imageUrl;
}
