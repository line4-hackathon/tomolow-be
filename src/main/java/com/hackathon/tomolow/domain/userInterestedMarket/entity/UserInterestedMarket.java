package com.hackathon.tomolow.domain.userInterestedMarket.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.hackathon.tomolow.domain.market.entity.Market;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.global.common.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
    name = "user_interested_market",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_user_market",
          columnNames = {"user_id", "market_id"})
    })
public class UserInterestedMarket extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // 관심 등록한 유저

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "market_id", nullable = false)
  private Market market; // 관심 등록한 주식
}
