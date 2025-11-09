package com.hackathon.tomolow.domain.group.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

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
    name = "group_info",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_group_code",
          columnNames = {"code"})
    })
public class Group extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 그룹명 */
  @Column(name = "name", nullable = false)
  private String name;

  /** 그룹 코드 (초대 코드 등으로 사용 가능) */
  @Column(name = "code", nullable = false, unique = true)
  private String code;

  /** 그룹 내 기준 인원수 */
  @Column(name = "member_count", nullable = false)
  private int memberCount;

  /** 기간 (예: 투자 기간, 단위: 주/일 등) */
  @Column(name = "duration", nullable = false)
  private int duration;

  /** 초기 자본 (시드머니) */
  @Column(name = "seed_money", nullable = false)
  private BigDecimal seedMoney;

  /** 그룹 내 자산 (현재 평가금액, null 가능) */
  @Column(name = "total_money")
  private BigDecimal totalMoney;

  /** 현재 활성화된 상태인지 (종료/인원부족 X) */
  @Column(name = "is_active")
  private Boolean isActive;

  /** 그룹을 생성한 사용자 */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "creator_id")
  private User creator;

  /** 활성화된 시간 */
  @Column(name = "is_active")
  private LocalDateTime activatedAt;

  /** 그룹 자산 갱신 */
  public void updateTotalMoney(BigDecimal totalMoney) {
    this.totalMoney = totalMoney;
  }

  public void addTotalMoney(BigDecimal money) { this.totalMoney.add(money); }

  /** 그룹 인원 증가 */
  public void increaseMemberCount() {
    this.memberCount++;
  }

  /** 그룹 인원 감소 */
  public void decreaseMemberCount() {
    if (this.memberCount > 0) {
      this.memberCount--;
    }
  }

  /** 그룹 활성화/비활성화 */
  public void setGroupActive(Boolean isActive) {
    this.isActive = isActive;
  }

  /** 그룹 활성화 시간 */
  public void setGroupActivatedAt(LocalDateTime activatedAt) {
    this.activatedAt = activatedAt;
  }

}
