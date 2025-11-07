package com.hackathon.tomolow.domain.transaction.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "PendingOrderCard DTO", description = "미체결(대기) 지정가 주문 카드 응답")
public class PendingOrderCardDto {

  @Schema(description = "주문 식별자(취소/정정에 사용)", example = "42f18bf6-3a5c-42b9-97e5-7b6d2e7d9d3a")
  private String orderId;

  @Schema(description = "마켓 ID", example = "2")
  private Long marketId;

  @Schema(description = "마켓 심볼", example = "KRW-ETH")
  private String symbol;

  @Schema(description = "마켓(종목) 이름", example = "이더리움")
  private String name;

  @Schema(description = "마켓 이미지 URL", example = "https://cdn.example.com/markets/eth.png")
  private String imageUrl;

  @Schema(description = "미체결 잔량(주문 수량 - 체결 수량)", example = "3")
  private int quantity;

  @Schema(description = "지정가(주문 가격)", example = "4975000.00")
  private BigDecimal limitPrice;
}
