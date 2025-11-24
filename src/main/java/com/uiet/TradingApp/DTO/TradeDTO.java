package com.uiet.TradingApp.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TradeDTO {
  private Long id;
  private String stockSymbol;
  private Long quantity;
  private BigDecimal price;
  private LocalDateTime timestamp;
  private String buyerUsername;
  private String sellerUsername;
}
