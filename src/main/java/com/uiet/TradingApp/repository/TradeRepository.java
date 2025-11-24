package com.uiet.TradingApp.repository;

import com.uiet.TradingApp.entity.Trade;
import com.uiet.TradingApp.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TradeRepository extends JpaRepository<Trade, Long> {
  @Query("SELECT t FROM Trade t WHERE t.buyer = :user OR t.seller = :user "
         + "ORDER BY t.timestamp DESC")
  List<Trade>
  findByUser(@Param("user") User user);
}
