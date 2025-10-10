package com.uiet.TradingApp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Entity
@Table(name = "trade")
@Data
@Builder
public class Trade {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private long id;

  @ManyToOne @JoinColumn(name = "buyer_id") private User buyer;

  @ManyToOne @JoinColumn(name = "seller_id") private User seller;

  @ManyToOne @JoinColumn(name = "stock_id") private Stock stock;

  @Column(nullable = false) private Long quantity;

  @Column(nullable = false, precision = 19, scale = 4) private BigDecimal price;

  private LocalDateTime timestamp;
}
