// TODO update stock price
package com.uiet.TradingApp.service;

import com.uiet.TradingApp.entity.Stock;
import com.uiet.TradingApp.entity.Trade;
import com.uiet.TradingApp.entity.User;
import com.uiet.TradingApp.repository.TradeRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TradeService {

  @Autowired private TradeRepository tradeRepository;
  @Autowired private UserService userService;
  @Autowired private PortfolioService portfolioService;

  @Transactional
  public void newEntry(Trade trade) {
    trade.setTimestamp(LocalDateTime.now());
    sendBalance(trade);
    addStocks(trade);
    tradeRepository.save(trade);
  }

  public Trade newTrade(User buyer, User seller, Stock stock, Long quantity,
                        BigDecimal price) {
    Trade newTrade = Trade.builder()
                         .buyer(buyer)
                         .seller(seller)
                         .stock(stock)
                         .quantity(quantity)
                         .price(price)
                         .build();
    return newTrade;
  }

  public void deleteEntry(Trade trade) { tradeRepository.delete(trade); }

  @Transactional
  public void sendBalance(Trade trade) {
    BigDecimal toSend =
        trade.getPrice().multiply(BigDecimal.valueOf(trade.getQuantity()));
    userService.addBalance(trade.getSeller(), toSend);
  }

  @Transactional
  public void addStocks(Trade trade) {
    portfolioService.addStocks(trade.getBuyer(), trade.getQuantity(),
                               trade.getStock());
  }
}
