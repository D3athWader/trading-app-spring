package com.uiet.TradingApp.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewPortfolio {
  private Long userId;
  private Long stockId;
}
