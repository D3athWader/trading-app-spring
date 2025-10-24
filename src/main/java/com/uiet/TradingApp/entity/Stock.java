package com.uiet.TradingApp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@Table(name = "stocks")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"company", "portfolio"})
public class Stock {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

  private BigDecimal totalPrice;

  private String symbol;
  private BigDecimal currentPrice;

  private BigDecimal openPrice;
  private BigDecimal closePrice;
  private BigDecimal highPrice;
  private BigDecimal lowPrice;

  private Long tradedVolume;
  private Long totalStocks;
  private LocalDateTime lastUpdated;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id")
  @JsonBackReference
  private Company company;
  @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL)
  private List<Portfolio> portfolio;

  public void setCompany(Company company) {
    this.company = company; // Sets this side
    if (company != null) {
      company.getStocks().add(this); // Automatically sets the other side
    }
  }

  @PrePersist
  @PreUpdate
  private void calculateTotalPrice() {
    this.totalPrice =
        this.currentPrice.multiply(BigDecimal.valueOf(this.totalStocks));
  }
}
