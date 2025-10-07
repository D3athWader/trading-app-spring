package com.uiet.TradingApp.service;

import com.uiet.TradingApp.entity.Stock;
import com.uiet.TradingApp.repository.StockRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StockService {
  @Autowired StockRepository stockRepository;

  public Optional<Stock> getStockBySymbol(String symbol) {
    return stockRepository.findBySymbol(symbol);
  }
}
