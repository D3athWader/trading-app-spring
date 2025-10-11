package com.uiet.TradingApp.service;

import com.uiet.TradingApp.entity.Enum.OrderStatus;
import com.uiet.TradingApp.entity.Enum.OrderType;
import com.uiet.TradingApp.entity.Order;
import com.uiet.TradingApp.entity.Stock;
import com.uiet.TradingApp.entity.User;
import com.uiet.TradingApp.repository.OrderRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

  @Autowired private OrderRepository orderRepository;
  @Autowired private UserService userService;
  // @Autowired private StockService stockService;
  @Autowired private PortfolioService portfolioService;
  @Autowired @Lazy OrderMatchingService orderMatchingService;

  @Transactional
  public boolean placeSellOrder(Order order) {
    Long stockQuantity = portfolioService.getUserStockQuantity(
        order.getUser(), order.getStock());
    if (stockQuantity >= order.getQuantity()) {
      order.setType(OrderType.SELL);
      order.setStatus(OrderStatus.PENDING);
      order.setTimestamp(LocalDateTime.now());
      portfolioService.removeStocks(order.getUser(), order.getQuantity(),
                                    order.getStock());
      orderRepository.save(order);
      orderMatchingService.sellOrderMatcher(order);
      return true;
    }
    return false;
  }

  @Transactional
  public void placeBuyOrder(Order order) {
    BigDecimal orderValue = order.getStock().getCurrentPrice().multiply(
        BigDecimal.valueOf(order.getQuantity()));
    if (orderValue.compareTo(userService.getUserBalance(order.getUser())) <=
        0) {
      order.setType(OrderType.BUY);
      order.setStatus(OrderStatus.PENDING);
      order.setTimestamp(LocalDateTime.now());
      order.setPrice(order.getStock().getCurrentPrice());
      userService.deductBalance(order.getUser(), orderValue);
      orderRepository.save(order);
      orderMatchingService.buyOrderMatcher(order);
    } else {
      throw new RuntimeException("Insufficient balance");
    }
  }

  public List<Order> ordersFromTime(LocalDateTime fromTime) {
    return orderRepository.findByTimestampGreaterThanEqualOrderByTimestampDesc(
        fromTime);
  }

  public List<Order> ordersFromUser(User user) {
    return orderRepository.findByUserOrderByTimestampDesc(user);
  }

  @Transactional
  public void cancelOrder(Order order) {
    OrderStatus status = order.getStatus();
    if (status.equals(OrderStatus.FILLED) ||
        status.equals(OrderStatus.CANCELLED)) {
      throw new RuntimeException("Order already filled or cancelled");
    } else {
      OrderType type = order.getType();
      order.setStatus(OrderStatus.CANCELLED);
      if (type.equals(OrderType.BUY)) {
        userService.addBalance(
            order.getUser(),
            order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())));
      } else {
        portfolioService.addStocks(order.getUser(), order.getQuantity(),
                                   order.getStock());
      }
    }
    orderRepository.save(order);
  }

  public List<Order> buyOrderMatcherHelper(Stock stock, OrderType type,
                                           List<OrderStatus> status,
                                           BigDecimal price) {
    return orderRepository
        .findByStockAndTypeAndStatusInAndPriceLessThanEqualOrderByPriceAsc(
            stock, type, status, price);
  }

  public List<Order> sellOrderMatcherHelper(Stock stock, OrderType type,
                                            List<OrderStatus> status,
                                            BigDecimal price) {
    return orderRepository
        .findByStockAndTypeAndStatusInAndPriceGreaterThanEqualOrderByPriceDesc(
            stock, type, status, price);
  }

  public void saveEntry(Order order) { orderRepository.save(order); }
}
