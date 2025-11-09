package com.hackathon.tomolow.domain.group.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.tomolow.domain.group.entity.Group;

public interface GroupRepository extends JpaRepository<Group, Long> {
  Optional<Group> findByCode(String code);

  boolean existsByCode(String code);

  boolean existsByName(String name);
}
