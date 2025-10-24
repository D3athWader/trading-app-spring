package com.uiet.TradingApp.service;

import com.uiet.TradingApp.entity.Enum.OrderStatus;
import com.uiet.TradingApp.entity.Enum.OrderType;
import com.uiet.TradingApp.entity.Order;
import com.uiet.TradingApp.entity.Stock;
import com.uiet.TradingApp.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderMatchingService {
  private final TradeService tradeService;
  private final OrderRepository orderRepository;

  @Transactional
  public void buyOrderMatcher(Order buyOrder) {

    Long neededStocks = buyOrder.getQuantity();
    Long fulfilledStocks = 0L;
    List<OrderStatus> status =
        List.of(OrderStatus.PENDING, OrderStatus.PARTIALLY_FILLED);
    List<Order> sellOrders = buyOrderMatcherHelper(
        buyOrder.getStock(), OrderType.SELL, status, buyOrder.getPrice());
    int i = 0;
    while (fulfilledStocks < neededStocks && i < sellOrders.size()) {
      Order sellOrder = sellOrders.get(i);
      Long currentOrderQuantity = sellOrder.getQuantity();
      if (currentOrderQuantity + fulfilledStocks <=
          neededStocks) { // FULLY FILLED sellOrder
        fulfilledStocks += currentOrderQuantity;
        sellOrder.setStatus(OrderStatus.FILLED);
        log.info("INFO: FULLY FILLED Sell Order {}", sellOrder.getId());
        orderRepository.save(sellOrder);
        tradeService.newEntry(tradeService.newTrade(
            buyOrder.getUser(), sellOrder.getUser(), buyOrder.getStock(),
            sellOrder.getQuantity(), sellOrder.getPrice()));

      } else if (currentOrderQuantity + fulfilledStocks >
                 neededStocks) { // PARTIALLY FILLED sellOrder
        sellOrder.setStatus(OrderStatus.PARTIALLY_FILLED);
        log.info("INFO: PARTIALLY FILLED Sell Order {}", sellOrder.getId());
        Long quantity = currentOrderQuantity - (neededStocks - fulfilledStocks);
        Long tradedQty = neededStocks - fulfilledStocks;
        sellOrder.setQuantity(quantity);
        orderRepository.save(sellOrder);
        tradeService.newEntry(tradeService.newTrade(
            buyOrder.getUser(), sellOrder.getUser(), buyOrder.getStock(),
            tradedQty, sellOrder.getPrice()));
        fulfilledStocks = neededStocks;
      }
      i++;
    }
    if (fulfilledStocks.equals(neededStocks)) {
      buyOrder.setStatus(OrderStatus.FILLED);
      log.info("INFO: FULLY FILLED Buy Order {}", buyOrder.getId());
    } else if (fulfilledStocks > 0L && fulfilledStocks < neededStocks) {
      buyOrder.setStatus(OrderStatus.PARTIALLY_FILLED);
      buyOrder.setQuantity(neededStocks - fulfilledStocks);
      log.info("INFO: PARTIALLY FILLED Buy Order {}", buyOrder.getId());
    }
    orderRepository.save(buyOrder);
  }

  @Transactional
  public void sellOrderMatcher(Order sellOrder) {
    Long sellingStocks = sellOrder.getQuantity();
    Long soldStocks = 0L;
    List<OrderStatus> status =
        List.of(OrderStatus.PENDING, OrderStatus.PARTIALLY_FILLED);
    List<Order> buyOrders = sellOrderMatcherHelper(
        sellOrder.getStock(), OrderType.BUY, status, sellOrder.getPrice());

    int i = 0;
    while (soldStocks < sellingStocks && i < buyOrders.size()) {
      Order buyOrder = buyOrders.get(i);
      Long currentOrderQuantity = buyOrder.getQuantity();
      if (currentOrderQuantity + soldStocks <= sellingStocks) { // FULLY FILLED
        buyOrder.setStatus(OrderStatus.FILLED);
        log.info("INFO: FULLY FILLED Buy Order {}", buyOrder.getId());
        orderRepository.save(buyOrder);
        tradeService.newEntry(tradeService.newTrade(
            buyOrder.getUser(), sellOrder.getUser(), buyOrder.getStock(),
            sellOrder.getQuantity(), sellOrder.getPrice()));
        soldStocks += currentOrderQuantity;
      } else if (currentOrderQuantity + soldStocks >
                 sellingStocks) { // PARTIALLY FILLED
        buyOrder.setStatus(OrderStatus.PARTIALLY_FILLED);
        log.info("INFO: PARTIALLY FILLED Buy Order {}", buyOrder.getId());
        buyOrder.setQuantity(currentOrderQuantity -
                             (sellingStocks - soldStocks));

        tradeService.newEntry(tradeService.newTrade(
            buyOrder.getUser(), sellOrder.getUser(), buyOrder.getStock(),
            sellOrder.getQuantity(), buyOrder.getPrice()));
        orderRepository.save(buyOrder);
        soldStocks = sellingStocks;
      }
      i++;
    }
    if (soldStocks.equals(sellingStocks)) {
      sellOrder.setStatus(OrderStatus.FILLED);
      log.info("INFO: FULLY FILLED Sell Order {}", sellOrder.getId());
    } else if (soldStocks > 0L && soldStocks < sellingStocks) {
      sellOrder.setStatus(OrderStatus.PARTIALLY_FILLED);
      sellOrder.setQuantity(sellingStocks - soldStocks);
      log.info("INFO: PARTIALLY FILLED Sell Order {}", sellOrder.getId());
    }
    orderRepository.save(sellOrder);
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
}
