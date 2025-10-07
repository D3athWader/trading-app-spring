package com.uiet.TradingApp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "portfolio")
public class Portfolio {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  private Long quantity;
  private Long averagePricePaid;
  @ManyToOne @JoinColumn(name = "user_id") private User user;
  @ManyToOne @JoinColumn(name = "stock_id") private Stock stock;
}
