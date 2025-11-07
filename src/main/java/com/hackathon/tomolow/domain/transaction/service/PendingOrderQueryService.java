package com.hackathon.tomolow.domain.transaction.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.market.repository.MarketRepository;
import com.hackathon.tomolow.domain.transaction.dto.PendingOrderCardDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PendingOrderQueryService {

  private final OrderRedisService orderRedisService;
  private final MarketRepository marketRepository;

  @Transactional(readOnly = true)
  public List<PendingOrderCardDto> getMyPendingOrders(Long userId) {
    // 1) 유저 미체결 후보
    Set<String> orderIds = orderRedisService.listUserOpenOrderIds(String.valueOf(userId));
    if (orderIds.isEmpty()) return List.of();

    // 2) 잔량 > 0 필터
    List<String> alive =
        orderIds.stream().filter(oid -> orderRedisService.getRemainingQuantity(oid) > 0).toList();
    if (alive.isEmpty()) return List.of();

    // 3) 주문 → marketId 모으기
    Map<String, Long> orderToMarket =
        alive.stream()
            .collect(
                Collectors.toMap(
                    oid -> oid,
                    oid -> {
                      String mid = orderRedisService.getOrderMarketId(oid);
                      return (mid == null || mid.isBlank()) ? null : Long.valueOf(mid);
                    }))
            .entrySet()
            .stream()
            .filter(e -> e.getValue() != null)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    if (orderToMarket.isEmpty()) return List.of();

    // 4) 마켓 일괄 로드
    Set<Long> marketIds = new HashSet<>(orderToMarket.values());
    Map<Long, Market> marketMap =
        marketRepository.findAllById(marketIds).stream()
            .collect(Collectors.toMap(Market::getId, m -> m));

    // 5) 카드로 매핑(주문 단위)
    List<PendingOrderCardDto> cards = new ArrayList<>();
    for (String oid : alive) {
      Long mid = orderToMarket.get(oid);
      if (mid == null) continue;
      Market m = marketMap.get(mid);
      if (m == null) continue;

      int remaining = orderRedisService.getRemainingQuantity(oid);
      if (remaining <= 0) continue;

      var price = orderRedisService.getPrice(oid); // 지정가

      cards.add(
          PendingOrderCardDto.builder()
              .orderId(oid)
              .marketId(m.getId())
              .symbol(m.getSymbol())
              .name(m.getName())
              .imageUrl(m.getImgUrl())
              .quantity(remaining) // ✅ 남은 수량
              .limitPrice(price) // ✅ 지정가
              .build());
    }

    // (선택) 최신 생성순/가격순 등 정렬 원하면 여기서 정렬
    return cards;
  }
}
