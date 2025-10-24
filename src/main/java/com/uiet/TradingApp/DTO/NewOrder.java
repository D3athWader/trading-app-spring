package com.uiet.TradingApp.DTO;

import com.uiet.TradingApp.entity.Enum.OrderType;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewOrder {
  private String stockSymbol;
  private Long quantity;
  private BigDecimal price;
  private OrderType type;
  private String username;
}
