package com.uiet.TradingApp.controller;

import com.uiet.TradingApp.DTO.ApiResponse;
import com.uiet.TradingApp.DTO.TradeDTO;
import com.uiet.TradingApp.service.TradeService;
import com.uiet.TradingApp.utils.JwtUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/trade")
public class TradeController {
  private final JwtUtil jwtUtil;
  private final TradeService tradeService;

  @GetMapping("/history")
  public ResponseEntity<ApiResponse<List<TradeDTO>>>
  getUserTradeHistory(@RequestHeader("Authorization") String authHeader) {
    try {
      String token = authHeader.substring(7);
      String username = jwtUtil.extractUsername(token);
      List<TradeDTO> trades = tradeService.getTradesForUser(username);
      return ResponseEntity.ok(new ApiResponse<>(trades));
    } catch (Exception e) {
      log.error("Error fetching trade history", e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new ApiResponse<>("Error fetching history: " + e.getMessage()));
    }
  }
}
