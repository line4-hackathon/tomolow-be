package com.hackathon.tomolow.domain.userStockHolding.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.tomolow.domain.stock.entity.Stock;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.userStockHolding.entity.UserStockHolding;

public interface UserStockHoldingRepository extends JpaRepository<UserStockHolding, Long> {
  Optional<UserStockHolding> findByUserAndStock(User user, Stock stock);
}
