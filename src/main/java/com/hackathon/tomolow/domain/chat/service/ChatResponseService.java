package com.hackathon.tomolow.domain.chat.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.tomolow.domain.chat.dto.AIChatResponseDto;
import com.hackathon.tomolow.domain.chat.dto.ChatRedisSaveDto;
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
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${CHAT_API_URL}")
  private String CHAT_API_URL;

  private static final Duration CACHE_TTL = Duration.ofHours(24);

  public ChatResponseDto getAIResponse(Long userId, ChatRequestDto chatRequestDto) {
    // GPT API에 응답 요청
    RestTemplate restTemplate = new RestTemplate();

    // cryptonews api 이용 위해 글로벌 심볼로 변환
    String tickers = chatRequestDto.getTickers();
    if (tickers.contains("-")) tickers = tickers.split("-")[1];
    chatRequestDto.updateTickers(tickers);

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
    String key = generateKey(userId, chatRequestDto.getQuestion());
    saveChatInRedis(userId, key, chatRequestDto.getQuestion(), aiChatResponseDto.getAnswer());

    return ChatResponseDto.builder()
        .answer(aiChatResponseDto.getAnswer())
        .sources(aiChatResponseDto.getSources())
        .key(key)
        .build();
  }

  private String generateKey(Long userId, String question) {
    return "CHAT:" + userId + ":" + question.hashCode() + UUID.randomUUID();
  }

  private void saveChatInRedis(Long userId, String key, String question, String answer) {
    ChatRedisSaveDto qna =
        ChatRedisSaveDto.builder().question(question).answer(answer).key(key).build();

    try {
      String json = objectMapper.writeValueAsString(qna);
      redisUtil.setData(key, json, CACHE_TTL);
      // key 목록을 리스트로 정리
      redisUtil.pushToList("CHAT_KEYS:" + userId, key);
    } catch (Exception e) {
      throw new CustomException(ChatErrorCode.JSON_MAPPING_ERROR);
    }
  }
}
