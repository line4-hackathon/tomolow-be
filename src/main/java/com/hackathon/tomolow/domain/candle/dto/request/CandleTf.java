package com.hackathon.tomolow.domain.candle.dto.request;

public enum CandleTf {
  D1, // 1일
  W1, // 1주
  M1, // 1개월
  M3,
  Y1; // 3개월

  public static CandleTf from(String v) {
    return CandleTf.valueOf(v.toUpperCase());
  }
}
