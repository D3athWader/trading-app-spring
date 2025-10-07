package com.uiet.TradingApp.repository;

import com.uiet.TradingApp.entity.Portfolio;
import com.uiet.TradingApp.entity.Stock;
import com.uiet.TradingApp.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
  public Optional<Portfolio> findByUserAndStock(User user, Stock stock);
}
