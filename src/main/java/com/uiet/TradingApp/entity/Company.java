package com.uiet.TradingApp.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@Table(name = "companies")
@AllArgsConstructor
@NoArgsConstructor
public class Company {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "user_id")
  private User user;
  private String name;
  private String tickerSymbol;
  private String sector;
  private BigDecimal marketCap;
  private LocalDateTime createdAt;
  @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
  @ToString.Exclude
  @JsonManagedReference(value = "company-stock")
  private List<Stock> stocks;
}
