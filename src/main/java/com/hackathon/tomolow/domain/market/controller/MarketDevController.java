package com.hackathon.tomolow.domain.market.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.tomolow.domain.market.dto.request.MarketCreateRequest;
import com.hackathon.tomolow.domain.market.dto.request.MarketUpdateRequest;
import com.hackathon.tomolow.domain.market.dto.response.MarketResponse;
import com.hackathon.tomolow.domain.market.service.MarketDevService;
import com.hackathon.tomolow.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Market", description = "종목(Market) 관리 API (개발자용)")
public class MarketDevController {

  private final MarketDevService marketDevService;

  @Operation(summary = "[개발자] 종목 생성", description = "새로운 종목을 등록합니다. (201 Created)")
  @PostMapping("/dev/market")
  public ResponseEntity<BaseResponse<MarketResponse>> create(
      @Valid @RequestBody MarketCreateRequest request) {
    MarketResponse response = marketDevService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BaseResponse.success("종목 등록 성공", response));
  }

  @Operation(summary = "[개발자] 종목 전체 조회", description = "모든 종목을 조회합니다. (200 OK)")
  @GetMapping("/dev/market")
  public ResponseEntity<BaseResponse<List<MarketResponse>>> list() {
    List<MarketResponse> list = marketDevService.findAll();
    return ResponseEntity.ok(BaseResponse.success("종목 목록 조회 성공", list));
  }

  @Operation(summary = "[개발자] 특정 종목 조회", description = "symbol로 단건 조회합니다. (200 OK)")
  @GetMapping("/dev/market/{symbol}")
  public ResponseEntity<BaseResponse<MarketResponse>> get(
      @Parameter(description = "종목 코드", example = "KRW-BTC") @PathVariable String symbol) {
    MarketResponse res = marketDevService.findOne(symbol);
    return ResponseEntity.ok(BaseResponse.success("종목 조회 성공", res));
  }

  @Operation(
      summary = "[개발자] 종목 정보 수정",
      description = "symbol 기준으로 이름, 이미지 URL, 자산/거래소를 수정합니다. (200 OK)")
  @PatchMapping("/dev/market")
  public ResponseEntity<BaseResponse<MarketResponse>> update(
      @Valid @RequestBody MarketUpdateRequest request) {
    MarketResponse response = marketDevService.update(request);
    return ResponseEntity.ok(BaseResponse.success("종목 정보 수정", response));
  }

  @Operation(summary = "[개발자] 종목 삭제", description = "symbol로 해당 종목을 삭제합니다. (200 OK)")
  @DeleteMapping("/dev/market/{symbol}")
  public ResponseEntity<BaseResponse<String>> delete(
      @Parameter(description = "종목 코드", example = "KRW-BTC") @PathVariable String symbol) {
    marketDevService.delete(symbol);
    return ResponseEntity.ok(BaseResponse.success("종목 삭제 완료"));
  }
}
