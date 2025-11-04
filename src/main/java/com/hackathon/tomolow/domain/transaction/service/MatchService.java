package com.hackathon.tomolow.domain.transaction.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackathon.tomolow.domain.stock.entity.Stock;
import com.hackathon.tomolow.domain.stock.exception.StockErrorCode;
import com.hackathon.tomolow.domain.stock.repository.StockRepository;
import com.hackathon.tomolow.domain.transaction.entity.TradeType;
import com.hackathon.tomolow.domain.transaction.entity.Transaction;
import com.hackathon.tomolow.domain.transaction.repository.TransactionRepository;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.user.exception.UserErrorCode;
import com.hackathon.tomolow.domain.user.repository.UserRepository;
import com.hackathon.tomolow.domain.userStockHolding.entity.UserStockHolding;
import com.hackathon.tomolow.domain.userStockHolding.exception.UserStockHoldingErrorCode;
import com.hackathon.tomolow.domain.userStockHolding.repository.UserStockHoldingRepository;
import com.hackathon.tomolow.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

  private final OrderRedisService orderRedisService;
  private final StockRepository stockRepository;
  private final UserRepository userRepository;
  private final TransactionRepository transactionRepository;
  private final UserStockHoldingRepository userStockHoldingRepository;

  @Transactional
  public void match(String stockId) {

    while (true) {
      // 1. 가장 높은 매수와 가장 낮은 매수 불러오기
      String buyOrderId = orderRedisService.getHighestBuy(stockId);
      String sellOrderId = orderRedisService.getLowestSell(stockId);

      if (buyOrderId == null || sellOrderId == null) break;

      BigDecimal buyPrice = orderRedisService.getPrice(buyOrderId);
      BigDecimal sellPrice = orderRedisService.getPrice(sellOrderId);

      // 2. 제일 높은 매수 가격 < 제일 낮은 매도 가격일 경우 체결 X
      if (buyPrice.compareTo(sellPrice) < 0) break;

      // 체결 ----------------
      // 3. 체결 가격, 수량 확인
      BigDecimal tradePrice = sellPrice;

      int buyRemainingQuantity = orderRedisService.getRemainingQuantity(buyOrderId);
      int sellRemainingQuantity = orderRedisService.getRemainingQuantity(sellOrderId);

      int tradeQuantity = Math.min(buyRemainingQuantity, sellRemainingQuantity);

      // 4. 잔여 수량 갱신
      orderRedisService.updateRemaining(buyOrderId, buyRemainingQuantity - tradeQuantity);
      orderRedisService.updateRemaining(sellOrderId, sellRemainingQuantity - tradeQuantity);

      // 5. 체결 내역 DB에 저장
      Stock stock =
          stockRepository
              .findById(Long.valueOf(stockId))
              .orElseThrow(() -> new CustomException(StockErrorCode.STOCK_NOT_FOUND));
      String buyUserId = orderRedisService.getUserId(buyOrderId);
      String sellUserId = orderRedisService.getUserId(sellOrderId);
      User buyUser =
          userRepository
              .findById(Long.valueOf(buyUserId))
              .orElseThrow(
                  () -> new CustomException(UserErrorCode.USER_NOT_FOUND, "해당 id의 유저가 존재하지 않습니다."));
      User sellUser =
          userRepository
              .findById(Long.valueOf(sellUserId))
              .orElseThrow(
                  () -> new CustomException(UserErrorCode.USER_NOT_FOUND, "해당 id의 유저가 존재하지 않습니다."));

      Transaction transaction =
          Transaction.builder()
              .stock(stock)
              .buyer(buyUser)
              .seller(sellUser)
              .quantity(tradeQuantity)
              .price(tradePrice)
              .build();
      transactionRepository.save(transaction);

      // 6. 사용자 주식 보유 수량 변화
      UserStockHolding buyUserStockHolding =
          userStockHoldingRepository
              .findByUserAndStock(buyUser, stock)
              .orElseGet(
                  () -> {
                    UserStockHolding newBuyUserStockHolding =
                        UserStockHolding.builder()
                            .stock(stock)
                            .user(buyUser)
                            .quantity(0L)
                            .avgPrice(BigDecimal.ZERO)
                            .build();
                    return userStockHoldingRepository.save(newBuyUserStockHolding);
                  });
      UserStockHolding sellUserStockHolding =
          userStockHoldingRepository
              .findByUserAndStock(sellUser, stock)
              .orElseThrow(() -> new CustomException(UserStockHoldingErrorCode.HOLDING_NOT_FOUND));
      buyUserStockHolding.addQuantity(tradeQuantity, tradePrice);
      sellUserStockHolding.subtractQuantity(tradeQuantity);

      // 7. 사용자 자산 변화
      BigDecimal totalPrice = tradePrice.multiply(BigDecimal.valueOf(tradeQuantity));
      buyUser.subtractCashBalance(totalPrice);
      sellUser.addCashBalance(totalPrice);
      buyUser.addInvestmentBalance(totalPrice);
      sellUser.addInvestmentBalance(totalPrice);

      // 8. 수량 소진된 주문은 Redis에서 제거
      if (buyRemainingQuantity - tradeQuantity == 0)
        orderRedisService.removeOrder(stockId, TradeType.BUY, buyOrderId);
      if (sellRemainingQuantity - tradeQuantity == 0)
        orderRedisService.removeOrder(stockId, TradeType.SELL, sellOrderId);

      // TODO: 9. 체결 정보를 웹소켓으로

      log.info("거래 체결 - buyOrderId : " + buyOrderId + ", sellOrderId : " + sellOrderId);
    }
  }
}
