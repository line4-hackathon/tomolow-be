package com.hackathon.tomolow.domain.chat.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.hackathon.tomolow.domain.chat.dto.AIChatResponseDto;
import com.hackathon.tomolow.domain.chat.dto.ChatRequestDto;
import com.hackathon.tomolow.domain.chat.dto.ChatResponseDto;
import com.hackathon.tomolow.domain.chat.exception.ChatErrorCode;
import com.hackathon.tomolow.global.exception.CustomException;
import com.hackathon.tomolow.global.redis.RedisUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatResponseService {

  private final RedisUtil redisUtil;

  @Value("${CHAT_API_URL}")
  private String CHAT_API_URL;

  private static final Duration CACHE_TTL = Duration.ofHours(24);

  public ChatResponseDto getAIResponse(Long userId, ChatRequestDto chatRequestDto) {
    // GPT API에 응답 요청
    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<ChatRequestDto> requestEntity = new HttpEntity<>(chatRequestDto, headers);

    AIChatResponseDto aiChatResponseDto;

    try {
      aiChatResponseDto =
          restTemplate
              .exchange(CHAT_API_URL, HttpMethod.POST, requestEntity, AIChatResponseDto.class)
              .getBody();

      if (aiChatResponseDto == null)
        throw new CustomException(ChatErrorCode.EXTERNAL_API_ERROR, "외부 API로부터 응답을 받아오지 못했습니다.");

    } catch (ResourceAccessException e) {
      throw new CustomException(ChatErrorCode.EXTERNAL_API_ERROR, "외부 API 연결에 실패했습니다.");
    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      throw new CustomException(ChatErrorCode.EXTERNAL_API_ERROR, "외부 API에서 알 수 없는 오류가 발생했습니다.");
    }

    // 질문과 응답을 Redis에 저장
    String key = generateCacheKey(userId, chatRequestDto.getQuestion());
    redisUtil.setData(key, aiChatResponseDto.getAnswer(), CACHE_TTL);

    return ChatResponseDto.builder()
        .answer(aiChatResponseDto.getAnswer())
        .sources(aiChatResponseDto.getSources())
        .key(key)
        .build();
  }

  private String generateCacheKey(Long userId, String question) {
    return "CHAT:" + userId + ":" + question.hashCode();
  }
}
