package com.uiet.TradingApp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "portfolio")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  private Long quantity;
  private BigDecimal averagePricePaid;
  @JsonBackReference @ManyToOne @JoinColumn(name = "user_id") private User user;
  @JsonBackReference
  @ManyToOne
  @JoinColumn(name = "stock_id")
  private Stock stock;
}
