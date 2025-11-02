package com.hackathon.tomolow.domain.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.tomolow.domain.group.entity.Group;

public interface GroupRepository extends JpaRepository<Group, Long> {}
