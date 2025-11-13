package com.hackathon.tomolow.domain.market.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.hackathon.tomolow.domain.market.dto.response.MarketPendingOrderResponseDto;
import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.market.exception.MarketErrorCode;
import com.hackathon.tomolow.domain.market.repository.MarketRepository;
import com.hackathon.tomolow.domain.transaction.service.OrderRedisService;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.user.exception.UserErrorCode;
import com.hackathon.tomolow.domain.user.repository.UserRepository;
import com.hackathon.tomolow.domain.userGroupTransaction.service.GroupOrderInfoService;
import com.hackathon.tomolow.domain.userGroupTransaction.service.GroupOrderRedisService;
import com.hackathon.tomolow.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MarketPendingOrderService {

  private final OrderRedisService orderRedisService;
  private final UserRepository userRepository;
  private final MarketRepository marketRepository;
  private final GroupOrderInfoService groupOrderInfoService;
  private final GroupOrderRedisService groupOrderRedisService;

  public List<MarketPendingOrderResponseDto> getMarketPendingOrders(Long userId, Long marketId) {

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    Market market =
        marketRepository
            .findById(marketId)
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));

    Set<String> ordersByMarketAndUser =
        orderRedisService.getOrdersByMarketAndUser(userId.toString(), marketId.toString());

    List<MarketPendingOrderResponseDto> dtos = new ArrayList<>();
    for (String orderId : ordersByMarketAndUser) {
      MarketPendingOrderResponseDto dto =
          MarketPendingOrderResponseDto.builder()
              .orderId(orderId)
              .imageUrl(market.getImgUrl())
              .quantity(orderRedisService.getRemainingQuantity(orderId))
              .tradeType(orderRedisService.getTradeType(orderId))
              .price(orderRedisService.getPrice(orderId))
              .build();
      dtos.add(dto);
    }

    return dtos;
  }

  public List<MarketPendingOrderResponseDto> getMarketUserGroupPendingOrders(
      Long userId, Long groupId, Long marketId) {

    groupOrderInfoService.getUserGroup(userId, groupId);
    Market market =
        marketRepository
            .findById(marketId)
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));

    Set<String> ordersByMarketAndUser =
        groupOrderRedisService.getOrdersByGroupAndMarketAndUser(
            userId.toString(), groupId.toString(), marketId.toString());

    List<MarketPendingOrderResponseDto> dtos = new ArrayList<>();
    for (String orderId : ordersByMarketAndUser) {
      MarketPendingOrderResponseDto dto =
          MarketPendingOrderResponseDto.builder()
              .orderId(orderId)
              .imageUrl(market.getImgUrl())
              .quantity(groupOrderRedisService.getRemainingQuantity(orderId, groupId.toString()))
              .tradeType(groupOrderRedisService.getTradeType(orderId, groupId.toString()))
              .price(groupOrderRedisService.getPrice(orderId, groupId.toString()))
              .build();
      dtos.add(dto);
    }
    return dtos;
  }
}
