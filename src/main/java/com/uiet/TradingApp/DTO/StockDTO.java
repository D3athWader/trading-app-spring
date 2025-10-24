package com.uiet.TradingApp.DTO;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockDTO {

  private Long id;
  private String symbol;
  private String companyName;
  private String sector;
  private BigDecimal currentPrice;
  private BigDecimal openPrice;
  private BigDecimal closePrice;
  private BigDecimal highPrice;
  private BigDecimal lowPrice;
  private Long tradedVolume;
}
