package com.uiet.TradingApp.DTO;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class NewStock {
  private String symbol;
  private BigDecimal openPrice;
  private Long totalStocks;
  private Long companyId;
}
