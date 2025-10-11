package com.uiet.TradingApp.controller;

import com.uiet.TradingApp.entity.User;
import com.uiet.TradingApp.service.UserService;
import com.uiet.TradingApp.utils.JwtUtil;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
public class PublicController {
  @Autowired private UserService userService;
  @Autowired private AuthenticationManager authenticationManager;
  @Autowired private UserDetailsService userDetailsService;
  @Autowired private JwtUtil jwtUtil;

  @GetMapping("/health-check")
  public String healthCheck() {
    return "Ok";
  }

  @PostMapping("/signup")
  public void signup(@RequestBody User user) {
    userService.createUser(user);
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody User user) {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(user.getUserName(),
                                                  user.getPassword()));
      UserDetails userDetails =
          userDetailsService.loadUserByUsername(user.getUserName());
      String jwtToken = jwtUtil.generateToken(userDetails.getUsername());
      return ResponseEntity.ok(jwtToken);

    } catch (Exception e) {
      return new ResponseEntity<>("Invalid username or password",
                                  HttpStatus.UNAUTHORIZED);
    }
  }
}
