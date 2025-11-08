package com.hackathon.tomolow.domain.chat.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class ChatSaveRequestDto {

  @NotNull private List<String> keys;
}
