package com.hackathon.tomolow.domain.market.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL) // ✅ null 필드는 JSON에서 제거
@Schema(description = "실시간 랭킹 아이템")
public class RankItem {

  @Schema(description = "마켓 ID", example = "1")
  private Long marketId;

  @Schema(description = "마켓 심볼", example = "KRW-BTC")
  private String symbol;

  @Schema(description = "마켓 이름", example = "비트코인")
  private String name;

  @Schema(description = "마켓 이미지 URL")
  private String imgUrl; // ✅ 추가

  @Schema(description = "현재가")
  private BigDecimal price;

  @Schema(description = "전일 대비 등락률 (0.0123 = +1.23%)")
  private BigDecimal changeRate;

  @Schema(description = "전일 대비 등락 원")
  private BigDecimal changePrice;

  @Schema(description = "관심등록 여부", example = "true")
  private Boolean interested; // ✅ Boolean로 바꾸면 null 가능, 🔸 개인화(REST 초기 1회에서만 채움, STOMP는 null)

  public static RankItem ofSymbolOnly(String s) {
    return RankItem.builder().symbol(s).name(s).price(BigDecimal.ZERO).build();
  }
}
