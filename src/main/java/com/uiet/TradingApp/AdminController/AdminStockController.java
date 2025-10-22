package com.uiet.TradingApp.AdminController;

import com.uiet.TradingApp.DTO.ApiResponse;
import com.uiet.TradingApp.DTO.NewStock;
import com.uiet.TradingApp.entity.Stock;
import com.uiet.TradingApp.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/admin/stock")

@RequiredArgsConstructor
public class AdminStockController {
  private final StockService stockService;
  private static final String ERROR_STRING = "ERROR: ";

  @PostMapping("/new-stock")
  public ResponseEntity<ApiResponse<Stock>>
  newStock(@RequestBody NewStock newStock) {
    try {
      Stock stock = stockService.createStockAndUpdate(newStock);
      return new ResponseEntity<>(new ApiResponse<>(stock), HttpStatus.CREATED);
    } catch (Exception e) {
      log.error("{} Failed to create stock {}", ERROR_STRING,
                newStock.getSymbol());
      return new ResponseEntity<>(new ApiResponse<>(ERROR_STRING + e),
                                  HttpStatus.BAD_REQUEST);
    }
  }

  @DeleteMapping("/delete-stock/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteStock(@PathVariable Long id) {
    try {
      stockService.deleteStock(id);
      log.info("INFO: Stock deleted successfully {}", id);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (Exception e) {
      log.error("ERROR: Failed to delete stock {}", id);
      log.error("ERROR: {}", e);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }
}
