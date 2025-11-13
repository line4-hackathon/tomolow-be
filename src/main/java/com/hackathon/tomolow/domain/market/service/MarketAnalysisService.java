package com.hackathon.tomolow.domain.market.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackathon.tomolow.domain.chat.dto.ChatRequestDto;
import com.hackathon.tomolow.domain.chat.dto.ChatResponseDto;
import com.hackathon.tomolow.domain.chat.service.ChatResponseService;
import com.hackathon.tomolow.domain.market.dto.request.MarketAnalysisRequestDto;
import com.hackathon.tomolow.domain.market.dto.response.MarketAnalysisResponseDto;
import com.hackathon.tomolow.domain.market.dto.response.MarketAnalysisSourceDto;
import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.market.exception.MarketErrorCode;
import com.hackathon.tomolow.domain.market.repository.MarketRepository;
import com.hackathon.tomolow.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MarketAnalysisService {

  @Value("${GET_NEWS_API_URL}")
  String GET_NEWS_API_URL;

  private final MarketRepository marketRepository;
  private final ChatResponseService chatResponseService;

  /** 마켓 상세 화면용 AI 주가 분석 - 뉴스 크롤링 + GPT 분석 (파이썬 서비스 재사용) */
  @Transactional(readOnly = true)
  public MarketAnalysisResponseDto analyzeMarket(
      Long userId, Long marketId, MarketAnalysisRequestDto priceContext) {

    Market market =
        marketRepository
            .findById(marketId)
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));

    BigDecimal current = priceContext.getCurrentPrice();
    BigDecimal previous = priceContext.getPreviousClosePrice();

    BigDecimal diff = current.subtract(previous);
    int cmp = diff.compareTo(BigDecimal.ZERO);

    String direction;
    if (cmp > 0) {
      direction = "상승";
    } else if (cmp < 0) {
      direction = "하락";
    } else {
      direction = "변동 없음";
    }

    String sign = (cmp >= 0 ? "+" : "-");

    // 프롬프트로 보낼 질문 (한국어, 1/2/3 구조 설명)
    String question =
        """
        당신은 금융 애널리스트입니다.

        종목: %s (%s)
        현재 가격은 %s원, 어제 종가는 %s원이라 %s%s원 %s한 상태입니다.

        아래 조건에 맞춰, 관련 뉴스들을 기반으로 분석해 주세요.

        1. 최근 1주일 내 %s 에 대한 뉴스 흐름을 2~3문장 정도로 간단히 요약해 주세요.
        2. 어제 종가 대비 현재 가격의 %s 요인(상승/하락의 배경이 되는 뉴스나 이벤트)을 정리해 주세요.
        3. 앞으로 주가 상승 또는 하락의 요인이 될 수 있는 뉴스/리스크가 있다면, 어떤 내용인지 정리해 주세요.

        답변은 반드시 다음 형식의 3개 섹션으로 나눠서 작성해 주세요.
        1. 뉴스 한줄 요약
        2. 현재 가격 움직임 분석
        3. 앞으로 주가에 영향을 줄 수 있는 요인

        모든 문장은 존댓말로 작성해 주세요.
        """
            .formatted(
                market.getName(),
                market.getSymbol(),
                current.toPlainString(),
                previous.toPlainString(),
                sign,
                diff.abs().toPlainString(),
                direction,
                market.getName(),
                direction);

    // 뉴스 API에서 쓰는 글로벌 심볼로 변환 (KRW-BTC -> BTC)
    String ticker = market.getSymbol();
    if (ticker.contains("-")) {
      ticker = ticker.split("-")[1];
    }

    // 파이썬 chat API 로 보낼 요청 (이미 쓰고 있는 ChatRequestDto 재사용)
    ChatRequestDto chatReq =
        ChatRequestDto.builder()
            .question(question)
            .data_selected(true) // "데이터 선택됨" → 뉴스 분석 모드
            .tickers(ticker) // 어떤 자산인지
            .start_date(LocalDate.now().minusDays(7).toString()) // 최근 1주일
            .end_date(LocalDate.now().toString())
            .build();

    // 기존 ChatResponseService 재사용 → Python /chat 호출
    ChatResponseDto chatResp = chatResponseService.getAIResponse(userId, chatReq);

    // Python 쪽에서 돌려준 sources 를 그대로 매핑
    List<MarketAnalysisSourceDto> sources =
        chatResp.getSources() == null
            ? List.of()
            : chatResp.getSources().stream()
                .map(
                    s ->
                        MarketAnalysisSourceDto.builder()
                            .title(s.getTitle())
                            .sourceName(s.getSource_name())
                            .url(s.getUrl())
                            .imageUrl(s.getImage_url())
                            .build())
                .toList();

    return MarketAnalysisResponseDto.builder()
        .analysis(chatResp.getAnswer()) // 1/2/3 섹션이 모두 들어있는 문자열
        .sources(sources) // 실제로 참고한 뉴스들만
        .build();
  }
}
