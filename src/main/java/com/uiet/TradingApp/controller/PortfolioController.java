package com.uiet.TradingApp.controller;

import com.uiet.TradingApp.DTO.ApiResponse;
import com.uiet.TradingApp.DTO.NewPortfolio;
import com.uiet.TradingApp.entity.Portfolio;
import com.uiet.TradingApp.service.PortfolioService;
import com.uiet.TradingApp.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portfolio")
@Slf4j
@RequiredArgsConstructor
public class PortfolioController {
  private final PortfolioService portfolioService;
  private final JwtUtil jwtUtil;

  @PostMapping("/create-portfolio")
  public ResponseEntity<ApiResponse<Portfolio>>
  createPortfolio(@RequestBody NewPortfolio newPortfolio,
                  @RequestHeader("Authorization") String authHeader) {
    log.info("Incoming portfolio: {}", newPortfolio);
    String token = authHeader.substring(7);
    String username = jwtUtil.extractUsername(token);
    String companyName = username.substring(5);
    try {
      log.info("INFO: Creating portfolio for user {}", username);
      log.info("INFO: Creating portfolio for stock {}",
               newPortfolio.getStockId());
      Portfolio portfolio = portfolioService.createEntry(newPortfolio);
      String companyNameOfTheStock =
          portfolio.getStock().getCompany().getName();
      if (!companyNameOfTheStock.equals(companyName)) {
        log.warn("User is of different company and the stock belongs to a "
                     + "different one username: {}, stock companyName: {}",
                 username, companyNameOfTheStock);
        return new ResponseEntity<>(
            new ApiResponse<>("User is of different company and the stock "
                              + "belongs to a different one"),
            HttpStatus.UNAUTHORIZED);
      }
      log.info("INFO: Portfolio created successfully for user {}", username);
      portfolioService.newEntry(portfolio);
      return new ResponseEntity<>(new ApiResponse<>(portfolio), HttpStatus.OK);
    } catch (Exception e) {
      log.error("ERROR: Failed to create portfolio for user {}", username);
      return new ResponseEntity<>(new ApiResponse<>(e.getMessage()),
                                  HttpStatus.BAD_REQUEST);
    }
  }
}
