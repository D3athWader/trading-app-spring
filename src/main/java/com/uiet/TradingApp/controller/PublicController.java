package com.uiet.TradingApp.controller;

import com.uiet.TradingApp.DTO.AuthRequest;
import com.uiet.TradingApp.entity.User;
import com.uiet.TradingApp.service.TempService;
import com.uiet.TradingApp.service.UserService;
import com.uiet.TradingApp.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
@Slf4j
public class PublicController {
  @Autowired private UserService userService;
  @Autowired private AuthenticationManager authenticationManager;
  @Autowired private UserDetailsService userDetailsService;
  @Autowired private JwtUtil jwtUtil;
  @Autowired private TempService tempService;

  @GetMapping("/health-check")
  public String healthCheck() {
    return "Ok";
  }

  @PostMapping("/signup")
  public ResponseEntity<User> createUser(@RequestBody User user) {
    userService.createUser(user);
    return new ResponseEntity<>(user, HttpStatus.CREATED);
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(authRequest.getUserName(),
                                                  authRequest.getPassword()));
      UserDetails userDetails =
          userDetailsService.loadUserByUsername(authRequest.getUserName());
      String jwtToken = jwtUtil.generateToken(userDetails.getUsername());
      return ResponseEntity.ok(jwtToken);

    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body("Login failed: " + e.getMessage());
    }
  }

  @GetMapping("/logout")
  public ResponseEntity<?>
  logout(@RequestHeader("Authorization") String authHeader) {
    String token = authHeader.substring(7);
    try {
      tempService.newEntry(token);
      log.info("logged out {}", token);
      return ResponseEntity.status(HttpStatus.OK).body("Logout successful");
    } catch (Exception e) {
      log.error("Error in logout {}", e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Error in logout");
    }
  }
}
