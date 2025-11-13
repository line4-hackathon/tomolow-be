package com.hackathon.tomolow.domain.userMarketHolding.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.userMarketHolding.entity.UserMarketHolding;

public interface UserMarketHoldingRepository extends JpaRepository<UserMarketHolding, Long> {

  Optional<UserMarketHolding> findByUserAndMarket(User user, Market market);

  // ✅ 내 보유 종목 전체 조회용
  List<UserMarketHolding> findAllByUser(User user);

  // 보유 여부만 빠르게 확인
  boolean existsByUser_IdAndMarket_Id(Long userId, Long marketId);

  boolean existsByUser_IdAndMarket_Symbol(Long userId, String symbol); // 심볼로도 지원
}
