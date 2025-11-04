package com.hackathon.tomolow.domain.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.tomolow.domain.stock.entity.Stock;

public interface StockRepository extends JpaRepository<Stock, Long> {}
