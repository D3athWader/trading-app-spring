package com.uiet.TradingApp.repository;

import com.uiet.TradingApp.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<Trade, Long> {}
