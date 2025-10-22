package com.uiet.TradingApp.service;

import com.uiet.TradingApp.entity.Portfolio;
import com.uiet.TradingApp.entity.Stock;
import com.uiet.TradingApp.entity.User;
import com.uiet.TradingApp.repository.PortfolioRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioService {
  private final PortfolioRepository portfolioRepository;

  public Long getUserStockQuantity(User user, Stock stock) {

    Optional<Portfolio> portfolio =
        portfolioRepository.findByUserAndStock(user, stock);
    if (portfolio.isPresent()) {
      return portfolio.get().getQuantity();
    }
    return 0L;
  }

  @Transactional
  public void removeStocks(User user, Long numberOfStocks, Stock stock) {
    Optional<Portfolio> opPortfolio =
        portfolioRepository.findByUserAndStock(user, stock);
    if (opPortfolio.isPresent()) {
      Portfolio portfolio = opPortfolio.get();
      if (numberOfStocks <= portfolio.getQuantity()) {
        portfolio.setQuantity(portfolio.getQuantity() - numberOfStocks);
        log.info("INFO: Removing {} stocks for user {}", numberOfStocks,
                 user.getUserName());
        portfolioRepository.save(portfolio);
        if (portfolio.getQuantity() == 0L) {
          log.info("INFO: Deleting portfolio {} for user {}", portfolio.getId(),
                   user.getUserName());
          portfolioRepository.delete(portfolio);
        }
      } else {
        log.error("ERROR: Not enough stocks for user {}", user.getUserName());
        throw new RuntimeException("Not enough stocks");
      }
    } else {
      log.error("ERROR: Portfolio not found for user {}", user.getUserName());
      throw new RuntimeException("Portfolio not found");
    }
  }

  @Transactional
  public void addStocks(User user, Long numberOfStocks, Stock stock) {
    Optional<Portfolio> portfolio =
        portfolioRepository.findByUserAndStock(user, stock);
    if (portfolio.isPresent()) {
      portfolio.get().setQuantity(portfolio.get().getQuantity() +
                                  numberOfStocks);
      log.info("INFO: Adding {} stocks for user {}", numberOfStocks,
               user.getUserName());
      portfolioRepository.save(portfolio.get());
    } else {
      Portfolio newPortfolio = Portfolio.builder()
                                   .user(user)
                                   .stock(stock)
                                   .quantity(numberOfStocks)
                                   .averagePricePaid(stock.getCurrentPrice())
                                   .build();
      log.info("INFO: Creating new portfolio for user {}", user.getUserName());
      portfolioRepository.save(newPortfolio);
    }
  }
}
