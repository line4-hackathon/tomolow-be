package com.hackathon.tomolow.domain.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.tomolow.domain.chat.entity.Chat;
import com.hackathon.tomolow.domain.user.entity.User;

public interface ChatRepository extends JpaRepository<Chat, Long> {
  boolean existsByUserAndKey(User user, String key);

  Optional<List<Chat>> findByUserId(Long userId);
}
