package com.uiet.TradingApp.controller;

import com.uiet.TradingApp.DTO.AuthRequest;
import com.uiet.TradingApp.entity.User;
import com.uiet.TradingApp.repository.UserRepository;
import com.uiet.TradingApp.service.EmailService;
import com.uiet.TradingApp.service.UserService;
import com.uiet.TradingApp.utils.JwtUtil;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
@Slf4j
public class PublicController {
  @Autowired private UserService userService;
  @Autowired private AuthenticationManager authenticationManager;
  @Autowired private UserDetailsService userDetailsService;
  @Autowired private JwtUtil jwtUtil;
  @Autowired private UserRepository userRepository;
  @Autowired private EmailService emailService;

  @GetMapping("/health-check")
  public String healthCheck() {
    return "Ok";
  }

  @PostMapping("/signup")
  public ResponseEntity<?> createUser(@RequestBody User user) {
    try {
      user.setVerified(false);
      userService.createUser(user);
      log.info("INFO: User created successfully {}", user.getUserName());
      return new ResponseEntity<>(
          "Verify your Email to continue using the site"
              + "\n"
              + "To verify POST /public/verify with your credentials",
          HttpStatus.CREATED);
    } catch (Exception e) {
      log.error("ERROR: Failed to create user {}", user.getUserName());
      return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }
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

  @Async
  @PostMapping("/verify")
  public ResponseEntity<?> verifyEmail(@RequestBody AuthRequest authRequest) {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(authRequest.getUserName(),
                                                  authRequest.getPassword()));
      UserDetails userDetails =
          userDetailsService.loadUserByUsername(authRequest.getUserName());
      User user =
          userRepository.findByUserName(userDetails.getUsername()).get();
      if (user.isVerified()) {
        return new ResponseEntity<>("Email already verified", HttpStatus.OK);
      }
      String verificationToken =
          jwtUtil.generateEmailVerificationToken(user.getEmail());
      user.setVerificationToken(verificationToken);
      emailService.sendVerificationEmail(user.getEmail(), verificationToken);
      userService.saveUser(user);
      return new ResponseEntity<>("Verification email sent!", HttpStatus.OK);
    } catch (Exception e) {
      log.error("Error in verify {}", e);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body("verification failed: " + e.getMessage());
    }
  }

  @Transactional
  @GetMapping("/verification")
  public ResponseEntity<?>
  verification(@RequestParam("token") String verificationToken) {
    try {

      String emailString = jwtUtil.extractUsername(verificationToken);
      Optional<User> opUser = userRepository.findByEmail(emailString);
      if (opUser.isEmpty() || opUser.get().getVerificationToken() == null) {
        return new ResponseEntity<>("No user found with this email",
                                    HttpStatus.FORBIDDEN);
      }
      if (opUser.get().isVerified()) {
        return new ResponseEntity<>("Email already verified",
                                    HttpStatus.FORBIDDEN);
      }
      if (!jwtUtil.validateToken(verificationToken) ||
          !opUser.get().getVerificationToken().equals(verificationToken)) {

        return new ResponseEntity<>("Token invalid or expired",
                                    HttpStatus.FORBIDDEN);
      }
      User user = opUser.get();
      user.setVerificationToken(null);
      user.setVerified(true);
      user.setRole(List.of("USER"));
      return new ResponseEntity<>("Email verified successfully", HttpStatus.OK);
    } catch (Exception e) {
      log.error("Error in verification {}", e);
      return new ResponseEntity<>(e, HttpStatus.UNAUTHORIZED);
    }
  }
}
