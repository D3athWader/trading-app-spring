package com.uiet.TradingApp.DTO;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class AuthRequest {

  private String username;
  private String password;
}
