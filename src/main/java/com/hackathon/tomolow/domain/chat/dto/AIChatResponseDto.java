package com.hackathon.tomolow.domain.chat.dto;

import java.util.List;

import lombok.Data;

@Data
public class AIChatResponseDto {

  private String answer;

  private List<NewsSource> sources;

  @Data
  public static class NewsSource {
    private String url;
    private String image_url;

    public NewsSource(String url, String image_url) {
      this.url = url;
      this.image_url = image_url;
    }
  }
}
