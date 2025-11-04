package com.uiet.TradingApp.DTO;

import io.micrometer.common.lang.NonNullFields;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@NonNullFields
public class OtpVerifyRequest {
  @NotNull(message = "userId cannot be null") private Long userId;
  @NotBlank(message = "Otp cannot be null") private String otp;
}
