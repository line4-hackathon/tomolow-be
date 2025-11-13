# 📈 TomoLow
### 투자는 모의로, 리스크는 로우로!
2025 멋쟁이사자처럼 4호선톤 16팀<br>
2025.10.11 ~ 2025.11.15

<br>
<h2>👀 Overview</h2>

**AI 뉴스 분석과 챗봇, 모의투자를 결합한 금융 학습 플랫폼**

기존의 투자 또는 모의투자 플랫폼에서는 사용자가 주식, ETF, 코인 등 실제로 투자하고 있는 상품의 학습 정보를 투자 맥락 안에서 쉽게 찾기 어렵습니다. 대부분 시세 확인과 거래 기능에 집중되어 있으며, 경제 요인이나 기업 분석, 시점별 이슈 등의 학습 컨텐츠는 별도로 분리되어있습니다. 제공되는 학습 자료 또한 영상이나 아티클 같은 일방향형 형태가 많아 실제 투자 과정에서 즉시 이해하거나 적용하기 어렵습니다. 이로 인해 사용자는 투자 중 발생하는 궁금증을 해소하지 못하고 단편적인 정보에 의존하게 되거나, 비체계적인 투자 패턴을 반복하게 됩니다.<br>
TomoLow는 실시간 가상화폐 시세를 기반으로 사용자가 직접 투자 경험을 쌓을 수 있도록 돕습니다. 동시에 종목별 최신 뉴스를 기반으로 AI가 핵심 내용을 요약하고 가격 변동 맥락을 설명해줍니다. 사용자는 챗봇을 통해 시점별 이슈와 경제적 개념을 대화 형태로 파고들며 이해도와 학습 몰입도를 높일 수 있으며, 그룹 모의투자 기능을 통해 친구들과 성과를 비교하며 학습 동기를 강화할 수 있습니다.<br>
TomoLow는 학습과 투자가 분리된 기존 구조를 개선하여, 사용자가 **'학습 -> 투자 -> 피드백 -> 재투자 -> 학습'** 구조 속에서 스스로 금융 이해력을 키울 수 있도록 돕습니다.

> ### 🎯 Target User
>- 투자 입문자 : 금융 용어나 지표 이해가 낮아 왜 오르고 내렸는지를 질문하며 알아보고 싶음
>- 모의투자 학습자 : 낮은 리스크로 실전 투자 경험을 쌓으며 학습한 내용을 실험해보고 싶음
>- 그룹형 학습자(동아리) : 간단하게 그룹 모의투자를 열어 참여자들끼리 선의의 경쟁을 해보고 싶음


<br><br>
<h2>🛠️ Tech Stack</h2>

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
<br>
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)
![Notion](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)
![Discord](https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white)


<br><br>
<h2>📌 Features</h2>

### 모의투자
- 실시간 가상화폐 시세를 기반으로 매매를 할 수 있는 모의투자를 진행합니다.
- 시장가 및 지정가 매수/매도 기능을 제공하며, 거래 내역과 손익률을 확인할 수 있습니다.
### 학습
- AI 챗봇에게 경제적 개념이나 특정 종목의 데이터를 기반으로 궁금한 점을 질문하며 학습힙니다.
- 차트에서 원하는 종목과 기간을 선택하면 RAG 방식으로 해당 기간 뉴스를 분석해 사용자의 질문에 대한 답변을 제공합니다.
- 도움이 된 채팅은 저장하여 학습 정보를 복습하 정리할 수 있습니다.
### 그룹 모의투자
- 멤버를 초대해 그룹으로 모의투자를 진행하며 선의의 경쟁을 유도합니다.
- 그룹에 참가 시 시드머니를 지불하고, 손익률 1위를 달성한 사용자 그룹 내 모든 참가자들의 시드머니를 획득하는 게이미피케이션 기능을 제공합니다.


<br><br>
<h2>👨‍👩‍👧‍👦 Members</h2>

|PM|FE|FE|FE|BE|BE|
|:--:|:--:|:--:|:--:|:--:|:--:|
|||||||
|동국대|숙명여대|동국대|서경대|서경대|숙명여대|
|박성준|박소연|박수연|정목진|윤해민|이경은|

<br><br>
<h2>📁 Project Structure</h2>

```

/src/main/java/com/hackathon/tomolow/domain
├── auth/                   // 회원가입 + 로그인 관련
├── candle/                 // 트레이딩 페이지용 캔들 조회
├── chat/                   // 채팅 관련
├── market/                 // 종목 관련
├── ticker/                 // 실시간 시세 조회
├── user/                   // 사용자 관련
├── transaction/            // 사용자의 매수 + 매도 + 거래내역
├── userGroup/              // 사용자가 가입한 그룹 관련
├── group/                  // 그룹 관련
└── userGroupTransaction    // 사용자의 그룹 내 매수 + 매도 + 거래 내역

```
