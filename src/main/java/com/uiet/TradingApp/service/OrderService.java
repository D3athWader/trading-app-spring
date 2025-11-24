package com.uiet.TradingApp.service;

import com.uiet.TradingApp.DTO.NewOrder;
import com.uiet.TradingApp.entity.Enum.OrderStatus;
import com.uiet.TradingApp.entity.Enum.OrderType;
import com.uiet.TradingApp.entity.Order;
import com.uiet.TradingApp.entity.Stock;
import com.uiet.TradingApp.entity.User;
import com.uiet.TradingApp.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;
  private final UserService userService;
  private final PortfolioService portfolioService;
  private final StockService stockService;
  private final OrderMatchingService orderMatchingService;

  @Transactional
  public boolean placeSellOrder(Order order) {
    Long stockQuantity = portfolioService.getUserStockQuantity(
        order.getUser(), order.getStock());
    if (stockQuantity >= order.getQuantity()) {
      order.setType(OrderType.SELL);
      order.setStatus(OrderStatus.PENDING);
      order.setTimestamp(LocalDateTime.now());
      log.info("INFO: Placing sell order for {}", order.getStock().getSymbol());
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
    log.info("Trying to place buy order");
    BigDecimal orderValue =
        order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
    BigDecimal userBalance =
        userService.getUserBalance(order.getUser().getUserName());
    log.info("User balance: {}, Order value: {}", userBalance, orderValue);
    if (orderValue.compareTo(userBalance) > 0) {
      throw new RuntimeException("Insufficient balance");
    }
    order.setType(OrderType.BUY);
    order.setStatus(OrderStatus.PENDING);
    order.setTimestamp(LocalDateTime.now());
    userService.deductBalance(order.getUser(), orderValue);
    log.info("INFO: Placing buy order for {}", order.getStock().getSymbol());
    orderRepository.save(order);
    orderMatchingService.buyOrderMatcher(order);
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
      log.error("ERROR: Order {} already filled or cancelled", order.getId());
      throw new RuntimeException("Order already filled or cancelled");
    } else {
      OrderType type = order.getType();
      order.setStatus(OrderStatus.CANCELLED);
      log.info("INFO: Cancelled order for {}", order.getStock().getSymbol());
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

  public void saveEntry(Order order) { orderRepository.save(order); }

  public Order createOrder(NewOrder newOrder) {
    Stock stock =
        stockService.getStockBySymbol(newOrder.getStockSymbol())
            .orElseThrow(() -> new RuntimeException("Stock not found"));
    User user = userService.getUserByUsername(newOrder.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
    return Order.builder()
        .stock(stock)
        .user(user)
        .quantity(newOrder.getQuantity())
        .price(newOrder.getPrice())
        .type(newOrder.getType())
        .status(OrderStatus.PENDING)
        .timestamp(LocalDateTime.now())
        .build();
  }
}
