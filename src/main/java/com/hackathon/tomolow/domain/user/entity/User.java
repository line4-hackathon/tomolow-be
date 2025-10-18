package com.hackathon.tomolow.domain.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hackathon.tomolow.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "user")
public class User extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "username", nullable = false)
  private String username;

  @JsonIgnore  // JSON 응답 시 password는 포함되지 않도록 설정
  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "nickname", nullable = false)
  private String nickname;

  @JsonIgnore
  @Column(name = "refresh_token")
  private String refreshToken;

  @Column(name = "role", nullable = false)  // "role" 컬럼과 매핑, Enum을 문자열로 저장
  @Enumerated(EnumType.STRING)
  @Builder.Default // Builder를 사용할 때 기본값으로 Role.USER 설정
  private Role role = Role.USER;

  // 리프레시 토큰 값을 설정하는 메서드 (토큰 재발급 시 사용)
  public void createRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public void updatePassword(String password) {
    this.password = password;
  }

}
