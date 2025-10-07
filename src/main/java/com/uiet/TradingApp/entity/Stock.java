package com.uiet.TradingApp.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Entity
@Data
@Table(name = "stocks")
public class Stock {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

  private String symbol;
  private BigDecimal currentPrice;

  private BigDecimal openPrice;
  private BigDecimal closePrice;
  private BigDecimal highPrice;
  private BigDecimal lowPrice;

  private Long tradedVolume;
  private LocalDateTime lastUpdated;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id")
  private Company company;
  @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL)
  private List<Portfolio> portfolio;
}
