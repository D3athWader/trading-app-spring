package com.uiet.TradingApp.service;

import com.uiet.TradingApp.DTO.NewStock;
import com.uiet.TradingApp.DTO.StockDTO;
import com.uiet.TradingApp.entity.Company;
import com.uiet.TradingApp.entity.Portfolio;
import com.uiet.TradingApp.entity.Stock;
import com.uiet.TradingApp.repository.StockRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {
  private final StockRepository stockRepository;

  public Optional<Stock> getStockBySymbol(String symbol) {
    log.info("INFO: Getting stock by symbol {}", symbol);
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
    log.info("INFO: Updating stock price for symbol {}", stock.getSymbol());
    stock.setLastUpdated(LocalDateTime.now());
    stockRepository.save(stock);
  }

  @Transactional
  public void updateTradedVolume(Stock stock, Long quantity) {
    stock.setTradedVolume(stock.getTradedVolume() + quantity);
    log.info("INFO: Updating stock traded volume for symbol {}",
             stock.getSymbol());
    stock.setLastUpdated(LocalDateTime.now());
    stockRepository.save(stock);
  }

  @Transactional
  public void saveEntry(Stock stock) {
    stock.setLastUpdated(LocalDateTime.now());
    log.info("INFO: Saving stock entry for symbol {}", stock.getSymbol());
    stockRepository.save(stock);
  }

  @Transactional
  public void newStock(Stock stock) {
    log.info("INFO: Creating new stock entry for symbol {}", stock.getSymbol());
    stockRepository.save(stock);
  }

  @Transactional
  public void changetotalStocks(Stock stock, Long newQuantity) {
    stock.setTotalStocks(newQuantity);
    log.info("INFO: Updating stock total stocks for symbol {}",
             stock.getSymbol());
    stock.setLastUpdated(LocalDateTime.now());
    stockRepository.save(stock);
  }

  public List<Portfolio> getPortfolios(Stock stock) {
    log.info("INFO: Getting stock portfolio for symbol {}", stock.getSymbol());
    return stock.getPortfolio();
  }

  public StockDTO dtoBuilder(Stock stock) {
    StockDTO stockDTO = StockDTO.builder()
                            .id(stock.getId())
                            .closePrice(stock.getClosePrice())
                            .currentPrice(stock.getCurrentPrice())
                            .highPrice(stock.getHighPrice())
                            .lowPrice(stock.getLowPrice())
                            .openPrice(stock.getOpenPrice())
                            .symbol(stock.getSymbol())
                            .tradedVolume(stock.getTradedVolume())
                            .companyName(stock.getCompany().getName())
                            .sector(stock.getCompany().getSector())
                            .build();
    log.info("INFO: Creating stock DTO for symbol {}", stock.getSymbol());
    return stockDTO;
  }

  public List<StockDTO> searchStocks(String companyName, String sector) {
    List<Stock> stocks;
    if (!companyName.equals("null") && !sector.equals("null")) {
      stocks =
          stockRepository
              .findByCompany_NameContainingIgnoreCaseAndCompany_SectorIgnoreCase(
                  companyName, sector);
      log.info("INFO: Searching stocks by company name {} and sector {}",
               companyName, sector);
    } else if (!companyName.equals("null")) {
      stocks =
          stockRepository.findByCompany_NameContainingIgnoreCase(companyName);
      log.info("INFO: Searching stocks by company name {}", companyName);
    } else if (!sector.equals("null")) {
      stocks = stockRepository.findByCompany_SectorIgnoreCase(sector);
      log.info("INFO: Searching stocks by sector {}", sector);
    } else {
      log.info("INFO: Searching all stocks");
      stocks = stockRepository.findAll();
    }
    return stocks.stream().map(this::dtoBuilder).toList();
  }

  @Transactional
  public void deleteStock(Long id) {
    stockRepository.deleteById(id);
  }

  public Stock buildStock(NewStock newStock, Company company) {
    BigDecimal openPrice = newStock.getOpenPrice();
    return Stock.builder()
        .symbol(newStock.getSymbol())
        .currentPrice(openPrice)
        .openPrice(openPrice)
        .highPrice(openPrice)
        .lowPrice(openPrice)
        .totalStocks(newStock.getTotalStocks())
        .tradedVolume(0L)
        .company(company)
        .portfolio(null)
        .build();
  }

  @Transactional
  public Stock createStockAndUpdate(NewStock newStock, Company company) {
    Stock stock = buildStock(newStock, company);
    stock.setLastUpdated(LocalDateTime.now());
    stockRepository.save(stock);
    log.info("INFO: Stock created successfully {}", newStock.getSymbol());
    return stock;
  }

  public Optional<Stock> getStockById(Long id) {
    log.info("INFO: Getting stock by id {}", id);
    return stockRepository.findById(id);
  }
}
