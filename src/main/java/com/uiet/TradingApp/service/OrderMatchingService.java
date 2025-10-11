// TODO transactions
package com.uiet.TradingApp.service;

import com.uiet.TradingApp.entity.Enum.OrderStatus;
import com.uiet.TradingApp.entity.Enum.OrderType;
import com.uiet.TradingApp.entity.Order;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderMatchingService {
  @Autowired private OrderService orderService;
  @Autowired private TradeService tradeService;

  @Transactional
  public void buyOrderMatcher(Order buyOrder) {

    Long neededStocks = buyOrder.getQuantity();
    Long fulfilledStocks = 0L;
    List<OrderStatus> status =
        List.of(OrderStatus.PENDING, OrderStatus.PARTIALLY_FILLED);
    List<Order> sellOrders = orderService.buyOrderMatcherHelper(
        buyOrder.getStock(), OrderType.SELL, status, buyOrder.getPrice());
    int i = 0;
    while (fulfilledStocks < neededStocks && i < sellOrders.size()) {
      Order sellOrder = sellOrders.get(i);
      Long currentOrderQuantity = sellOrder.getQuantity();
      if (currentOrderQuantity + fulfilledStocks <=
          neededStocks) { // FULLY FILLED sellOrder
        fulfilledStocks += currentOrderQuantity;
        sellOrder.setStatus(OrderStatus.FILLED);
        orderService.saveEntry(sellOrder);
        tradeService.newEntry(tradeService.newTrade(
            buyOrder.getUser(), sellOrder.getUser(), buyOrder.getStock(),
            sellOrder.getQuantity(), sellOrder.getPrice()));

      } else if (currentOrderQuantity + fulfilledStocks >
                 neededStocks) { // PARTIALLY FILLED sellOrder
        sellOrder.setStatus(OrderStatus.PARTIALLY_FILLED);
        Long quantity = currentOrderQuantity - (neededStocks - fulfilledStocks);
        Long tradedQty = neededStocks - fulfilledStocks;
        sellOrder.setQuantity(quantity);
        orderService.saveEntry(sellOrder);
        tradeService.newEntry(tradeService.newTrade(
            buyOrder.getUser(), sellOrder.getUser(), buyOrder.getStock(),
            tradedQty, sellOrder.getPrice()));
        fulfilledStocks = neededStocks;
      }
      i++;
    }
    if (fulfilledStocks == neededStocks) {
      buyOrder.setStatus(OrderStatus.FILLED);
    } else if (fulfilledStocks > 0L && fulfilledStocks < neededStocks) {
      buyOrder.setStatus(OrderStatus.PARTIALLY_FILLED);
      buyOrder.setQuantity(neededStocks - fulfilledStocks);
    }
    orderService.saveEntry(buyOrder);
  }

  @Transactional
  public void sellOrderMatcher(Order sellOrder) {
    Long sellingStocks = sellOrder.getQuantity();
    Long soldStocks = 0L;
    List<OrderStatus> status =
        List.of(OrderStatus.PENDING, OrderStatus.PARTIALLY_FILLED);
    List<Order> buyOrders = orderService.sellOrderMatcherHelper(
        sellOrder.getStock(), OrderType.BUY, status, sellOrder.getPrice());

    int i = 0;
    while (soldStocks < sellingStocks && i < buyOrders.size()) {
      Order buyOrder = buyOrders.get(i);
      Long currentOrderQuantity = buyOrder.getQuantity();
      if (currentOrderQuantity + soldStocks <= sellingStocks) { // FULLY FILLED
        buyOrder.setStatus(OrderStatus.FILLED);
        orderService.saveEntry(buyOrder);
        tradeService.newEntry(tradeService.newTrade(
            buyOrder.getUser(), sellOrder.getUser(), buyOrder.getStock(),
            sellOrder.getQuantity(), sellOrder.getPrice()));
        soldStocks += currentOrderQuantity;
      } else if (currentOrderQuantity + soldStocks >
                 sellingStocks) { // PARTIALLY FILLED
        buyOrder.setStatus(OrderStatus.PARTIALLY_FILLED);
        buyOrder.setQuantity(currentOrderQuantity -
                             (sellingStocks - soldStocks));

        tradeService.newEntry(tradeService.newTrade(
            buyOrder.getUser(), sellOrder.getUser(), buyOrder.getStock(),
            sellOrder.getQuantity(), buyOrder.getPrice()));
        orderService.saveEntry(buyOrder);
        soldStocks = sellingStocks;
      }
      i++;
    }
    if (soldStocks == sellingStocks) {
      sellOrder.setStatus(OrderStatus.FILLED);
    } else if (soldStocks > 0L && soldStocks < sellingStocks) {
      sellOrder.setStatus(OrderStatus.PARTIALLY_FILLED);
      sellOrder.setQuantity(sellingStocks - soldStocks);
    }
    orderService.saveEntry(sellOrder);
  }
}
