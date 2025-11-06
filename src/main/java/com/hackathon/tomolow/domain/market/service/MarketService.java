package com.hackathon.tomolow.domain.market.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackathon.tomolow.domain.market.dto.request.MarketCreateRequest;
import com.hackathon.tomolow.domain.market.dto.request.MarketUpdateRequest;
import com.hackathon.tomolow.domain.market.dto.response.MarketResponse;
import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.market.exception.MarketErrorCode;
import com.hackathon.tomolow.domain.market.mapper.MarketMapper;
import com.hackathon.tomolow.domain.market.repository.MarketRepository;
import com.hackathon.tomolow.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketService {

  private final MarketRepository marketRepository;
  private final MarketMapper marketMapper;

  @Transactional
  public MarketResponse create(MarketCreateRequest req) {
    if (marketRepository.existsBySymbol(req.getSymbol())) {
      throw new CustomException(MarketErrorCode.MARKET_ALREADY_EXISTS);
    }
    Market saved =
        marketRepository.save(
            Market.builder()
                .name(req.getName())
                .symbol(req.getSymbol())
                .assetType(req.getAssetType())
                .exchangeType(req.getExchangeType())
                .imgUrl(req.getImgUrl())
                .build());
    log.info("[Market 생성] symbol={}, name={}", saved.getSymbol(), saved.getName());
    return marketMapper.toResponse(saved);
  }

  public List<MarketResponse> findAll() {
    return marketRepository.findAll().stream().map(marketMapper::toResponse).toList();
  }

  public MarketResponse findOne(String symbol) {
    Market market =
        marketRepository
            .findBySymbol(symbol)
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));
    return marketMapper.toResponse(market);
  }

  @Transactional
  public MarketResponse update(MarketUpdateRequest req) {
    Market market =
        marketRepository
            .findBySymbol(req.getSymbol())
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));

    if (req.getNewName() != null) {
      market.setName(req.getNewName());
    }
    if (req.getNewImgUrl() != null) {
      market.setImgUrl(req.getNewImgUrl());
    }
    if (req.getNewAssetType() != null) {
      market.setAssetType(req.getNewAssetType());
    }
    if (req.getNewExchangeType() != null) {
      market.setExchangeType(req.getNewExchangeType());
    }

    log.info(
        "[Market 수정] symbol={}, name={}, assetType={}, exchangeType={}",
        market.getSymbol(),
        market.getName(),
        market.getAssetType(),
        market.getExchangeType());

    return marketMapper.toResponse(market);
  }

  @Transactional
  public void delete(String symbol) {
    Market market =
        marketRepository
            .findBySymbol(symbol)
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));
    marketRepository.delete(market);
    log.info("[Market 삭제] symbol={}", symbol);
  }
}
