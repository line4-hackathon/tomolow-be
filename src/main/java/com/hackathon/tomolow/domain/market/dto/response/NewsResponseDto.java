package com.hackathon.tomolow.domain.market.dto.response;

import lombok.Data;

@Data
public class NewsResponseDto {

  private String title;

  private String url;

  private String image_url;

  private String source_name;
}
