package com.uiet.TradingApp.repository;

import com.uiet.TradingApp.entity.Company;
import com.uiet.TradingApp.entity.Stock;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {
  public Optional<Stock> findBySymbol(String symbol);

  public List<Stock> findByCurrentPrice(Double price);

  public List<Stock> findByCompany(Company company);
}
