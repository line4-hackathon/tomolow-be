package com.hackathon.tomolow.domain.ticker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.tomolow.domain.ticker.dto.TickerMessage;
import com.hackathon.tomolow.global.redis.RedisUtil;
import com.hackathon.tomolow.global.response.BaseResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ticker")
public class TickerController {

  private final RedisUtil redisUtil;
  private final ObjectMapper om;

  @GetMapping("/{market}")
  public ResponseEntity<?> getLastTicker(@PathVariable String market) throws Exception {
    String json = redisUtil.getData("ticker:" + market);
    if (json == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(
        BaseResponse.success("실시간 시세 받아오기에 성공했습니다.", om.readValue(json, TickerMessage.class)));

    // return ResponseEntity.ok(om.readValue(json, TickerMessage.class));
  }
}
