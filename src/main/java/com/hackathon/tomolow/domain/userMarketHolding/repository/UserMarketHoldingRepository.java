package com.hackathon.tomolow.domain.userStockHolding.repository;

import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.userStockHolding.entity.UserStockHolding;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStockHoldingRepository extends JpaRepository<UserStockHolding, Long> {

  Optional<UserStockHolding> findByUserAndStock(User user, Market stock);
}
