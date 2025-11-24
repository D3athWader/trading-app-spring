// DONE: update stock price
package com.uiet.TradingApp.service;

import com.uiet.TradingApp.DTO.TradeDTO;
import com.uiet.TradingApp.entity.Company;
import com.uiet.TradingApp.entity.Stock;
import com.uiet.TradingApp.entity.Trade;
import com.uiet.TradingApp.entity.User;
import com.uiet.TradingApp.repository.TradeRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeService {

  private final TradeRepository tradeRepository;
  private final UserService userService;
  private final PortfolioService portfolioService;
  private final StockService stockService;
  private final CompanyService companyService;
  private final SimpMessagingTemplate messagingTemplate;

  @Transactional
  public void newEntry(Trade trade) {
    trade.setTimestamp(LocalDateTime.now());
    tradeRepository.save(trade);
    sendBalance(trade);
    addStocks(trade);
    log.info("INFO: Creating new trade entry for symbol {}", trade.getStock());
    setPrices(trade);
    broadcastTrade(trade);
  }

  public Trade newTrade(User buyer, User seller, Stock stock, Long quantity,
                        BigDecimal price) {
    Trade newTrade = Trade.builder()
                         .buyer(buyer)
                         .seller(seller)
                         .stock(stock)
                         .quantity(quantity)
                         .price(price)
                         .sentBalance(false)
                         .sentStocks(false)
                         .build();
    log.info("INFO: Creating new trade entry for symbol {}", stock.getSymbol());
    return newTrade;
  }

  public void deleteEntry(Trade trade) {
    log.info("INFO: Deleting trade entry for symbol {}", trade.getStock());
    tradeRepository.delete(trade);
  }

  public List<TradeDTO> getTradesForUser(String username) {
    User user = userService.getUserByUsername(username).orElseThrow(
        () -> new RuntimeException("User not found"));
    List<Trade> trades = tradeRepository.findByUser(user);
    return trades.stream().map(this::convertToDTO).toList();
  }

  private void broadcastTrade(Trade trade) {
    try {
      TradeDTO dto = convertToDTO(trade);
      // Sending to public topic
      messagingTemplate.convertAndSend("/topic/trades", dto);
      log.info("INFO: Broadcasted trade for {}", trade.getStock().getSymbol());
    } catch (Exception e) {
      log.error("ERROR: Failed to broadcast trade", e);
    }
  }

  private TradeDTO convertToDTO(Trade trade) {
    return TradeDTO.builder()
        .id(trade.getId())
        .stockSymbol(trade.getStock().getSymbol())
        .quantity(trade.getQuantity())
        .price(trade.getPrice())
        .timestamp(trade.getTimestamp())
        .buyerUsername(trade.getBuyer().getUserName())
        .sellerUsername(trade.getSeller().getUserName())
        .build();
  }

  private void sendBalance(Trade trade) {

    BigDecimal toSend =
        trade.getPrice().multiply(BigDecimal.valueOf(trade.getQuantity()));
    log.info("INFO: Sending balance {} to {}", toSend, trade.getSeller());
    userService.addBalance(trade.getSeller(), toSend);
    trade.setSentBalance(true);
  }

  private void addStocks(Trade trade) {
    log.info("INFO: Adding stocks quantity {} for user {}", trade.getQuantity(),
             trade.getBuyer());
    portfolioService.addStocks(trade.getBuyer(), trade.getQuantity(),
                               trade.getStock());
    trade.setSentStocks(true);
  }

  private void setPrices(Trade trade) {
    log.info("INFO: Updating price for stock {}", trade.getStock());
    Stock stock = trade.getStock();
    stock.setCurrentPrice(trade.getPrice());
    if (trade.getPrice().compareTo(trade.getStock().getHighPrice()) > 0) {
      log.info("INFO: Updating high price for stock {}", trade.getStock());
      stock.setHighPrice(trade.getPrice());
    }
    if (trade.getPrice().compareTo(trade.getStock().getLowPrice()) < 0) {
      log.info("INFO: Updating low price for stock {}", trade.getStock());
      stock.setLowPrice(trade.getPrice());
    }
    Company company = stock.getCompany();
    company.setMarketCap(stock.getCurrentPrice().multiply(
        BigDecimal.valueOf(stock.getTotalStocks())));
    log.info("INFO: Updating market cap for company {}", company.getName());
    companyService.saveEntry(company);
    stockService.saveEntry(stock);
  }
}
