package com.hackathon.tomolow.domain.group.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

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
  private int seedMoney;

  /** 그룹 내 자산 (현재 평가금액, null 가능) */
  @Column(name = "total_money")
  private Integer totalMoney;

  /** 그룹 자산 갱신 */
  public void updateTotalMoney(int totalMoney) {
    this.totalMoney = totalMoney;
  }

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
}
