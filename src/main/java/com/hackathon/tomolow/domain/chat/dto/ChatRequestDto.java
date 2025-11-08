package com.hackathon.tomolow.domain.chat.dto;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class ChatRequestDto {

  @NotNull private String question;

  @NotNull private Boolean data_selected;

  private String tickers;

  private String start_date;

  private String end_date;
}
