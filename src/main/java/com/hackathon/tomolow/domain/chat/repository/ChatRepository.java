package com.hackathon.tomolow.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.tomolow.domain.chat.entity.Chat;

public interface ChatRepository extends JpaRepository<Chat, Long> {}
