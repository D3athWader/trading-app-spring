package com.uiet.TradingApp.DTO;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class NewPortfolio {
  private Long userId;
  private Long stockId;
}
