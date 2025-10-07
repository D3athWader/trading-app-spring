package com.uiet.TradingApp.repository;

import com.uiet.TradingApp.entity.Company;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
  public Optional<Company> findByTickerSymbol(String tickerSymbol);

  public Optional<Company> findByName(String name);

  public List<Company> findBySector(String sector);
}
