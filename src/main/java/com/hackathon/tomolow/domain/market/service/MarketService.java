package com.hackathon.tomolow.domain.market.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.hackathon.tomolow.domain.chat.exception.ChatErrorCode;
import com.hackathon.tomolow.domain.market.dto.response.NewsResponseDto;
import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.market.exception.MarketErrorCode;
import com.hackathon.tomolow.domain.market.repository.MarketRepository;
import com.hackathon.tomolow.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MarketService {

  @Value("${GET_NEWS_API_URL}")
  String GET_NEWS_API_URL;

  private final MarketRepository marketRepository;

  public List<NewsResponseDto> getRecentNews(Long marketId) {
    Market market =
        marketRepository
            .findById(marketId)
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));

    String symbol = market.getSymbol();

    // 글로벌 심볼로 파싱
    if (symbol.contains("-")) symbol = symbol.split("-")[1];

    // FastAPI에 요청
    String requestUrl = GET_NEWS_API_URL + symbol;

    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

    List<NewsResponseDto> newsList;

    try {
      newsList =
          restTemplate
              .exchange(
                  requestUrl,
                  HttpMethod.GET,
                  requestEntity,
                  new ParameterizedTypeReference<List<NewsResponseDto>>() {})
              .getBody();

      if (newsList == null)
        throw new CustomException(ChatErrorCode.EXTERNAL_API_ERROR, "외부 API로부터 응답을 받아오지 못했습니다.");

    } catch (ResourceAccessException e) {
      throw new CustomException(ChatErrorCode.EXTERNAL_API_ERROR, "외부 API 연결에 실패했습니다.");
    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      System.out.println(e.getMessage());
      throw new CustomException(ChatErrorCode.EXTERNAL_API_ERROR, "외부 API에서 알 수 없는 오류가 발생했습니다.");
    }

    return newsList;
  }
}
