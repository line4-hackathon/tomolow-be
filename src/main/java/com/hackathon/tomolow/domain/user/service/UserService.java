package com.hackathon.tomolow.domain.user.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackathon.tomolow.domain.user.dto.response.TopUpResponse;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

  private final UserRepository userRepository;

  @Transactional
  public TopUpResponse topUpFixed(User user, BigDecimal amount) {
    user.addCashBalance(amount);

    userRepository.save(user);

    BigDecimal total = user.getCashBalance().add(user.getInvestmentBalance());
    return TopUpResponse.builder()
        .cashBalance(user.getCashBalance())
        .investmentBalance(user.getInvestmentBalance())
        .totalAsset(total)
        .build();
  }
}
