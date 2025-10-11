package com.uiet.TradingApp.service;

import com.uiet.TradingApp.entity.Portfolio;
import com.uiet.TradingApp.entity.Stock;
import com.uiet.TradingApp.entity.User;
import com.uiet.TradingApp.repository.PortfolioRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PortfolioService {
  @Autowired private PortfolioRepository portfolioRepository;

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
        portfolioRepository.save(portfolio);
        if (portfolio.getQuantity() == 0L) {
          portfolioRepository.delete(portfolio);
        }
      } else {
        throw new RuntimeException("Not enough stocks");
      }
    } else {
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
      portfolioRepository.save(portfolio.get());
    } else {
      Portfolio newPortfolio = Portfolio.builder()
                                   .user(user)
                                   .stock(stock)
                                   .quantity(numberOfStocks)
                                   .averagePricePaid(stock.getCurrentPrice())
                                   .build();
      portfolioRepository.save(newPortfolio);
    }
  }
}
