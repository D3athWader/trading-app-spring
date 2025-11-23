package com.uiet.TradingApp.DTO;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioDTO {
  private String stockSymbol;
  private Long quantity;
  private BigDecimal avgPrice;
  private BigDecimal currentPrice;
  private BigDecimal totalValue;
}
