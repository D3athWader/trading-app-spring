package com.uiet.TradingApp.controller;

import com.uiet.TradingApp.DTO.ApiResponse;
import com.uiet.TradingApp.DTO.OtpVerifyRequest;
import com.uiet.TradingApp.service.CustomUserDetailsService;
import com.uiet.TradingApp.service.TotpService;
import com.uiet.TradingApp.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("totp")
@RequiredArgsConstructor
public class TotpController {
  private final TotpService totpService;
  private final CustomUserDetailsService userDetailsService;
  private final JwtUtil jwtUtil;

  @PostMapping("/verify")
  public ResponseEntity<ApiResponse<Void>>
  verifyOtp(@RequestBody OtpVerifyRequest body) {
    boolean verifyToken =
        totpService.verifyToken(body.getUserId(), body.getOtp());
    if (verifyToken) {
      UserDetails userDetails =
          userDetailsService.loadUserByUserId(body.getUserId());
      String jwtToken = jwtUtil.generateToken(userDetails.getUsername());
      return ResponseEntity.ok(new ApiResponse<>(jwtToken));
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ApiResponse<>("Invalid token"));
    }
  }

  @PostMapping("/setup")
  public ResponseEntity<ApiResponse<Boolean>>
  setupOtp(@RequestBody OtpVerifyRequest body) {
    return ResponseEntity.ok(new ApiResponse<>(
        totpService.enableTotp(body.getUserId(), body.getOtp())));
  }
}
