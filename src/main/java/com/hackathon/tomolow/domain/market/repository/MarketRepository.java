package com.hackathon.tomolow.domain.market.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.tomolow.domain.market.entity.ExchangeType;
import com.hackathon.tomolow.domain.market.entity.Market;

public interface MarketRepository extends JpaRepository<Market, Long> {

  Optional<Market> findBySymbol(String symbol);

  Optional<Market> findByName(String name);

  boolean existsBySymbol(String symbol);

  // 업비트에 등록된 마켓만 가져오기
  List<Market> findAllByExchangeType(ExchangeType exchangeType);

  List<Market> findAllBySymbolIn(Iterable<String> symbols);

  List<Market> findByNameContainingIgnoreCaseOrSymbolContainingIgnoreCase(
      String name, String symbol);
}
