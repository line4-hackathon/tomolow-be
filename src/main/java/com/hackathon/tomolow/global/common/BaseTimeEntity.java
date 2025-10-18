package com.hackathon.tomolow.global.common;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class) //JPA의 감시기능을 가능하게 하는 어노테이션
public abstract class BaseTimeEntity {

  @CreatedDate  // 엔티티가 처음 저장될 때, 생성일을 자동으로 기록
  private LocalDateTime createdAt;

  @LastModifiedDate // 엔티티가 수정될 때마다, 자동으로 갱신
  private LocalDateTime updatedAt;

}