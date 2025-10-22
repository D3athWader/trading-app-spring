package com.uiet.TradingApp.controller;

import com.uiet.TradingApp.DTO.StockDTO;
import com.uiet.TradingApp.entity.Stock;
import com.uiet.TradingApp.repository.StockRepository;
import com.uiet.TradingApp.service.StockService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// TODO: create stocks admin controller
// TODO: LATER add pagination to searches to improve performance in case of high
// number of stocks
@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {
  private final StockService stockService;
  private final StockRepository stockRepository;

  @GetMapping("/{id}")
  public ResponseEntity<?> getStockById(@PathVariable Long id) {
    Optional<Stock> stock = stockRepository.findById(id);
    if (!stock.isPresent()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    StockDTO stockDTO = stockService.dtoBuilder(stock.get());
    return new ResponseEntity<>(stockDTO, HttpStatus.OK);
  }

  @GetMapping("/search")
  public ResponseEntity<?>
  searchStocks(@RequestParam(required = false) String name,
               @RequestParam(required = false) String sector) {
    List<StockDTO> stocks = stockService.searchStocks(name, sector);
    if (stocks.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(stocks, HttpStatus.OK);
  }
}
