package com.uiet.TradingApp.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDTO {

  private Long id;
  private String userName;
  private String country;
}
