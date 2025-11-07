package com.hackathon.tomolow.domain.market.mapper;

import org.springframework.stereotype.Component;

import com.hackathon.tomolow.domain.market.dto.response.MarketResponse;
import com.hackathon.tomolow.domain.market.entity.Market;

@Component
public class MarketMapper {

  public MarketResponse toResponse(Market m) {
    return MarketResponse.builder()
        .id(m.getId())
        .name(m.getName())
        .symbol(m.getSymbol())
        .assetType(m.getAssetType())
        .exchangeType(m.getExchangeType())
        .imgUrl(m.getImgUrl())
        .build();
  }
}
