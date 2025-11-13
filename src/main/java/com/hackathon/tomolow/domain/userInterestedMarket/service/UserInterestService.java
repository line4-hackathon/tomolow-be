package com.hackathon.tomolow.domain.userInterestedMarket.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.market.exception.MarketErrorCode;
import com.hackathon.tomolow.domain.market.repository.MarketRepository;
import com.hackathon.tomolow.domain.ticker.dto.TickerMessage;
import com.hackathon.tomolow.domain.ticker.service.PriceQueryService;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.user.exception.UserErrorCode;
import com.hackathon.tomolow.domain.user.repository.UserRepository;
import com.hackathon.tomolow.domain.userInterestedMarket.dto.InterestToggleResponse;
import com.hackathon.tomolow.domain.userInterestedMarket.dto.InterestedMarketCard;
import com.hackathon.tomolow.domain.userInterestedMarket.entity.UserInterestedMarket;
import com.hackathon.tomolow.domain.userInterestedMarket.repository.UserInterestedMarketRepository;
import com.hackathon.tomolow.global.exception.CustomException;
import com.hackathon.tomolow.global.redis.RedisUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserInterestService {

  private final UserRepository userRepository;
  private final MarketRepository marketRepository;
  private final UserInterestedMarketRepository interestRepo;
  private final PriceQueryService priceQueryService;
  private final RedisUtil redisUtil;

  /** 하트 토글: 없으면 생성, 있으면 삭제 */
  @Transactional
  public InterestToggleResponse toggle(Long userId, Long marketId) {
    var exists = interestRepo.existsByUser_IdAndMarket_Id(userId, marketId);
    if (exists) {
      interestRepo.deleteByUser_IdAndMarket_Id(userId, marketId);
      return new InterestToggleResponse(false, marketId);
    }

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    Market market =
        marketRepository
            .findById(marketId)
            .orElseThrow(() -> new CustomException(MarketErrorCode.MARKET_NOT_FOUND));

    var entity = UserInterestedMarket.builder().user(user).market(market).build();
    interestRepo.save(entity);

    return new InterestToggleResponse(true, marketId);
  }

  /** 내 관심 마켓 목록 */
  @Transactional(readOnly = true)
  public List<InterestedMarketCard> list(Long userId) {
    return interestRepo.findAllByUser_IdOrderByCreatedAtDesc(userId).stream()
        .map(
            im -> {
              var market = im.getMarket();
              BigDecimal price = BigDecimal.ZERO;
              BigDecimal changeRate = BigDecimal.ZERO;

              try {
                // ✅ Redis에서 실시간 시세 조회
                String json = redisUtil.getData("ticker:" + market.getSymbol());
                if (json != null) {
                  TickerMessage ticker = new ObjectMapper().readValue(json, TickerMessage.class);
                  price = ticker.getTradePrice();
                  changeRate = ticker.getChangeRate();
                } else {
                  // fallback: PriceQueryService 사용
                  price = priceQueryService.getLastTradePriceOrThrow(market.getSymbol());
                }
              } catch (Exception ignored) {
              }

              return InterestedMarketCard.builder()
                  .marketId(im.getMarket().getId())
                  .symbol(im.getMarket().getSymbol())
                  .name(im.getMarket().getName())
                  .imageUrl(im.getMarket().getImgUrl())
                  .price(price)
                  .changeRate(changeRate)
                  .build();
            })
        .toList();
  }

  /** 특정 마켓이 관심인지 단건 조회 (마켓 리스트 화면에서 하트 ON/OFF 표시에 필요하면 사용) */
  @Transactional(readOnly = true)
  public boolean isInterested(Long userId, Long marketId) {
    return interestRepo.existsByUser_IdAndMarket_Id(userId, marketId);
  }
}
