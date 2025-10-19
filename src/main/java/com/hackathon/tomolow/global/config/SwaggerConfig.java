package com.hackathon.tomolow.global.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("📈 투모로우(TomoLow) API 명세서")
                .version("1.0")
                .description(
                    """
                    <h2>투자를 모의로, 리스크는 로우로!</h2>
                    <p><strong>TomoLow</strong>는 여러분들의 자산의 미래를 책임질 모의투자 학습 플랫폼입니다.<br>

                    <h3>서비스 설명</h3>
                    <p>
                    기존 투자/모의투자 플랫폼은 거래와 시세 확인에 집중되어 있어, 사용자가 학습할 기회를 얻기 어렵습니다.<br>
                    TomoLow는 <strong>투자 경험과 학습 경험을 하나의 순환 구조로 연결</strong>하여,<br>
                    단순한 숫자와 그래프를 넘어 <strong>맥락 기반 학습</strong>을 지원합니다.
                    </p>

                    <h3>문제 상황</h3>
                    <ul>
                        <li>현재 플랫폼들은 경제 요인, 기업 분석, 시점별 이슈 같은 학습 콘텐츠가 분리되어 있어, 투자 맥락과 학습 경험이 단절되어 있습니다.</li>
                        <li>사용자는 주가 급등·급락 구간의 원인을 이해하기 어렵고, 단편적 정보(커뮤니티, 소문, 단기 뉴스)에 의존하게 됩니다.</li>
                        <li>학습한 내용을 투자 과정과 연결하지 못해 <strong>비체계적 학습</strong>과 <strong>감정적 투자</strong>가 반복됩니다.</li>
                    </ul>

                    <h3>투모로우의 해결 방향</h3>
                    <ul>
                        <li><strong>시점 기반 탐색</strong>: 차트의 특정 시점을 클릭하면, 해당 구간의 주가 변동 원인을 학습할 수 있습니다.</li>
                        <li><strong>AI 대화형 탐구</strong>: "왜 이 시점에 급등했어?" 같은 꼬리질문으로 맥락 있는 학습이 가능합니다.</li>
                        <li><strong>포트폴리오 분석</strong>: AI가 포트폴리오와 시장 데이터를 종합적으로 분석해 학습자에게 객관적 피드백을 제공합니다.</li>
                        <li><strong>경쟁과 협력</strong>: 투자 학습 그룹 내에서 모의투자 성과를 비교하며 동기부여를 높일 수 있습니다.</li>
                        <li><strong>실전 적용</strong>: 배운 내용을 즉시 모의투자에 반영하여 리스크 없이 실전 감각을 익힐 수 있습니다.</li>
                    </ul>

                    <p><strong>TomoLow</strong>는 단순한 모의투자 서비스가 아닙니다.<br>
                    투자와 학습을 잇는 새로운 경험을 제공하는 <strong>미래지향적 투자 학습 플랫폼</strong>입니다.</p>
                    """)
                .contact(
                    new Contact()
                        .name("투모로우")
                        .url("https://tomolow.store")
                        .email("yhaemin0531@naver.com")));
  }

  @Bean
  public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder()
        .group("Swagger API") // API 그룹명
        .pathsToMatch("/api/**", "/swagger-ui/**", "/v3/api-docs/**")
        .build();
  }
}
