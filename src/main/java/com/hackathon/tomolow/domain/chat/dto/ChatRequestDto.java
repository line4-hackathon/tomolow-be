package com.hackathon.tomolow.domain.chat.dto;

import jakarta.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatRequestDto {

  @NotNull private String question;

  @NotNull private Boolean data_selected;

  private String tickers;

  private String start_date;

  private String end_date;

  public void updateTickers(String tickers) {
    this.tickers = tickers;
  }
}
