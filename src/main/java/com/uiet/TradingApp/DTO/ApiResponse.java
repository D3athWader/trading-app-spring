package com.uiet.TradingApp.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
  private String message;
  private T data;

  public ApiResponse(String message) {
    this.message = message;
    this.data = null;
  }

  public ApiResponse(T data) {
    this.message = null;
    this.data = data;
  }
}
