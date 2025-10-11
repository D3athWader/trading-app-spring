package com.uiet.TradingApp.service;

import com.uiet.TradingApp.DTO.StockDTO;
import com.uiet.TradingApp.entity.Portfolio;
import com.uiet.TradingApp.entity.Stock;
import com.uiet.TradingApp.repository.StockRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StockService {
  @Autowired StockRepository stockRepository;

  public Optional<Stock> getStockBySymbol(String symbol) {
    return stockRepository.findBySymbol(symbol);
  }

  @Transactional
  public void updatePrice(Stock stock, BigDecimal soldPrice) {
    stock.setCurrentPrice(soldPrice);
    if (soldPrice.compareTo(stock.getHighPrice()) > 1) {

      stock.setHighPrice(soldPrice);
    }
    if (soldPrice.compareTo(stock.getLowPrice()) < 1) {

      stock.setLowPrice(soldPrice);
    }
    saveEntry(stock);
  }

  @Transactional
  public void updateTradedVolume(Stock stock, Long quantity) {
    stock.setTradedVolume(stock.getTradedVolume() + quantity);
    saveEntry(stock);
  }

  @Transactional
  public void saveEntry(Stock stock) {
    stock.setLastUpdated(LocalDateTime.now());
    stockRepository.save(stock);
  }

  @Transactional
  public void newStock(Stock stock) {
    stock.setOpenPrice(stock.getCurrentPrice());
    stock.setTradedVolume(0L);
    stock.setClosePrice(BigDecimal.valueOf(0.0));
  }

  @Transactional
  public void changetotalStocks(Stock stock, Long newQuantity) {
    stock.setTotalStocks(newQuantity);
    saveEntry(stock);
  }

  public List<Portfolio> getPortfolios(Stock stock) {
    return stock.getPortfolio();
  }

  public StockDTO dtoBuilder(Stock stock) {
    StockDTO stockDTO = StockDTO.builder()
                            .closePrice(stock.getClosePrice())
                            .currentPrice(stock.getCurrentPrice())
                            .highPrice(stock.getHighPrice())
                            .lowPrice(stock.getLowPrice())
                            .openPrice(stock.getOpenPrice())
                            .symbol(stock.getSymbol())
                            .tradedVolume(stock.getTradedVolume())
                            .company_name(stock.getCompany().getName())
                            .sector(stock.getCompany().getSector())
                            .build();
    return stockDTO;
  }

  public List<StockDTO> searchStocks(String companyName, String sector) {
    List<Stock> stocks;
    if (!companyName.equals("null") && !sector.equals("null")) {
      stocks =
          stockRepository
              .findByCompany_NameContainingIgnoreCaseAndCompany_SectorIgnoreCase(
                  companyName, sector);
    } else if (!companyName.equals("null")) {
      stocks =
          stockRepository.findByCompany_NameContainingIgnoreCase(companyName);
    } else if (!sector.equals("null")) {
      stocks = stockRepository.findByCompany_SectorIgnoreCase(sector);
    } else {
      stocks = stockRepository.findAll();
    }
    return stocks.stream().map(this::dtoBuilder).toList();
  }
}
