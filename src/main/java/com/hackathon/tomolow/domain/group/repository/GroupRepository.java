package com.hackathon.tomolow.domain.group.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hackathon.tomolow.domain.group.entity.Group;

public interface GroupRepository extends JpaRepository<Group, Long> {
  Optional<Group> findByCode(String code);

  boolean existsByCode(String code);

  boolean existsByName(String name);

  @Query("SELECT g FROM Group g " + "WHERE g.isActive = true " + "AND g.endAt <= :now")
  List<Group> findExpiredGroups(LocalDateTime now);
}
