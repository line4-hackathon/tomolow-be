package com.hackathon.tomolow.domain.userGroupStockHolding.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.userGroup.entity.UserGroup;
import com.hackathon.tomolow.domain.userGroupStockHolding.entity.UserGroupMarketHolding;

public interface UserGroupMarketHoldingRepository
    extends JpaRepository<UserGroupMarketHolding, Long> {
  Optional<List<UserGroupMarketHolding>> findByUserGroup_Id(Long id);

  Optional<UserGroupMarketHolding> findByUserGroup_IdAndMarket_Id(Long userGroupId, Long marketId);

  Optional<UserGroupMarketHolding> findByUserGroupAndMarket(UserGroup userGroup, Market market);
}
