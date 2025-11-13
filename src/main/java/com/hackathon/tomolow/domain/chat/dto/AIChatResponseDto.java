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
    private String title;
    private String source_name;

    public NewsSource(String url, String image_url, String title, String source_name) {
      this.url = url;
      this.image_url = image_url;
      this.title = title;
      this.source_name = source_name;
    }
  }
}
