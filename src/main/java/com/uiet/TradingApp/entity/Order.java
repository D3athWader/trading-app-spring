package com.uiet.TradingApp.entity;

import com.uiet.TradingApp.entity.Enum.OrderStatus;
import com.uiet.TradingApp.entity.Enum.OrderType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders")
public class Order {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @ManyToOne @JoinColumn(name = "user_id") private User user;

  @ManyToOne @JoinColumn(name = "stock_id") private Stock stock;
  @Column(nullable = false) private Long quantity;

  @Column(nullable = false, precision = 19, scale = 4) private BigDecimal price;

  @Enumerated(EnumType.STRING) @Column(nullable = false) private OrderType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status;

  private LocalDateTime timestamp;
  private Long remainingQuantity;
}
