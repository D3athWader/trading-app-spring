package com.uiet.TradingApp.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Entity
@Data
@Table(name = "companies")
public class Company {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
  private String tickerSymbol;
  private String sector;
  // Long shares;
  private BigDecimal marketCap;
  private LocalDateTime createdAt;
  @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
  private List<Stock> stocks;
}
