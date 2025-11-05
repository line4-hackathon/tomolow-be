package com.hackathon.tomolow.domain.userMarketHolding.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.userMarketHolding.entity.UserMarketHolding;

public interface UserMarketHoldingRepository extends JpaRepository<UserMarketHolding, Long> {

  Optional<UserMarketHolding> findByUserAndMarket(User user, Market market);
}
