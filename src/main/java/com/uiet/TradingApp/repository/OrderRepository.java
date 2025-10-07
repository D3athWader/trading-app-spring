package com.uiet.TradingApp.repository;

import com.uiet.TradingApp.entity.Enum.OrderStatus;
import com.uiet.TradingApp.entity.Enum.OrderType;
import com.uiet.TradingApp.entity.Order;
import com.uiet.TradingApp.entity.Stock;
import com.uiet.TradingApp.entity.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
  public List<Order>
  findByTimestampGreaterThanEqualOrderByTimestampDesc(LocalDateTime timeStamp);

  public List<Order> findByUserOrderByTimestampDesc(User user);

  public List<Order>
  findByStockAndTypeAndStatusInAndPriceLessThanEqualOrderByPriceAsc(
      Stock stock, OrderType type, List<OrderStatus> status, BigDecimal price);
}
