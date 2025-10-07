// TODO Add Trade Entries
package com.uiet.TradingApp.service;

import com.uiet.TradingApp.entity.Enum.OrderStatus;
import com.uiet.TradingApp.entity.Enum.OrderType;
import com.uiet.TradingApp.entity.Order;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderMatchingService {
  @Autowired private OrderService orderService;

  public void buyOrderMatcher(Order buyOrder) {

    Long neededStocks = buyOrder.getQuantity();
    Long fulfilledStocks = 0L;
    List<OrderStatus> status =
        List.of(OrderStatus.PENDING, OrderStatus.PARTIALLY_FILLED);
    List<Order> sellOrders = orderService.orderMatcherHelper(
        buyOrder.getStock(), OrderType.SELL, status, buyOrder.getPrice());
    int i = 0;
    while (fulfilledStocks < neededStocks && i < sellOrders.size()) {
      Long currentOrderQuantity = sellOrders.get(i).getQuantity();
      if (currentOrderQuantity + fulfilledStocks <=
          neededStocks) { // FULLY FILLED sellOrder
        fulfilledStocks += currentOrderQuantity;
        sellOrders.get(i).setStatus(OrderStatus.FILLED);
        orderService.saveEntry(sellOrders.get(i));
      } else if (currentOrderQuantity + fulfilledStocks >
                 neededStocks) { // PARTIALLY FILLED sellOrder
        sellOrders.get(i).setStatus(OrderStatus.PARTIALLY_FILLED);
        sellOrders.get(i).setQuantity(currentOrderQuantity -
                                      (neededStocks - fulfilledStocks));
        orderService.saveEntry(sellOrders.get(i));
        fulfilledStocks = neededStocks;
      }
      i++;
    }
    if (fulfilledStocks == neededStocks) {
      buyOrder.setStatus(OrderStatus.FILLED);
    }
    orderService.saveEntry(buyOrder);
  }

  public void priceChecker() {}

  public void fillChecker(Order order) {}
}
