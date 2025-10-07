package com.uiet.TradingApp.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyDTO {
  private String name;
  private String tickerSymbol;
  private String sector;
  private Double marketCap;
}
