package com.uiet.TradingApp.controller;

import com.uiet.TradingApp.DTO.ApiResponse;
import com.uiet.TradingApp.DTO.AuthRequest;
import com.uiet.TradingApp.entity.User;
import com.uiet.TradingApp.repository.UserRepository;
import com.uiet.TradingApp.service.EmailService;
import com.uiet.TradingApp.service.UserService;
import com.uiet.TradingApp.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/public")
@Slf4j
@RequiredArgsConstructor
public class PublicController {
  private final UserService userService;
  private final AuthenticationManager authenticationManager;
  private final UserDetailsService userDetailsService;
  private final JwtUtil jwtUtil;
  private final UserRepository userRepository;
  private final EmailService emailService;
  private static final String ERROR_STRING = "ERROR: ";

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
  public ResponseEntity<ApiResponse<Void>>
  login(@RequestBody AuthRequest authRequest) {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(authRequest.getUsername(),
                                                  authRequest.getPassword()));
      UserDetails userDetails =
          userDetailsService.loadUserByUsername(authRequest.getUsername());
      String jwtToken = jwtUtil.generateToken(userDetails.getUsername());
      return ResponseEntity.ok(new ApiResponse<>(jwtToken));

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ApiResponse<>(ERROR_STRING +
                                  "Login failed: " + e.getMessage()));
    }
  }

  @Async
  @PostMapping("/verify")
  public CompletableFuture<ResponseEntity<ApiResponse<Void>>>
  verifyEmail(@RequestBody AuthRequest authRequest,
              HttpServletRequest request) {
    String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                         .replacePath(null)
                         .build()
                         .toUriString();
    return CompletableFuture.supplyAsync(() -> {
      try {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(authRequest.getUsername(),
                                                    authRequest.getPassword()));

        UserDetails userDetails =
            userDetailsService.loadUserByUsername(authRequest.getUsername());
        Optional<User> opUser =
            userRepository.findByUserName(userDetails.getUsername());
        if (opUser.isEmpty()) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(new ApiResponse<>("User not found"));
        }
        User user = opUser.get();
        if (user.isVerified()) {
          return ResponseEntity.ok(new ApiResponse<>("Email already verified"));
        }
        String verificationToken =
            jwtUtil.generateEmailVerificationToken(user.getEmail());
        user.setVerificationToken(verificationToken);
        emailService.sendVerificationEmail(user.getEmail(), verificationToken,
                                           baseUrl);
        userService.saveUser(user);
        return ResponseEntity.ok(new ApiResponse<>("Verification email sent!"));
      } catch (Exception e) {
        log.error("Error in verifyEmail: ", e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ApiResponse<>("Verification failed: " + e.getMessage()));
      }
    });
  }

  @Transactional
  @GetMapping("/verification")
  public ResponseEntity<ApiResponse<Void>>
  verification(@RequestParam("token") String verificationToken) {
    try {

      String emailString = jwtUtil.extractUsername(verificationToken);
      Optional<User> opUser = userRepository.findByEmail(emailString);
      String verificationTokenInDb = opUser.get().getVerificationToken();
      if (opUser.isEmpty() || verificationTokenInDb == null) {
        return new ResponseEntity<>(
            new ApiResponse<>("No user found with this email"),
            HttpStatus.FORBIDDEN);
      }
      if (opUser.get().isVerified()) {
        return new ResponseEntity<>(new ApiResponse<>("Email already verified"),
                                    HttpStatus.FORBIDDEN);
      }
      boolean validateToken = jwtUtil.validateToken(verificationToken);
      boolean equals = verificationTokenInDb.equals(verificationToken);
      if (!validateToken || !equals) {

        return new ResponseEntity<>(
            new ApiResponse<>("Token invalid or expired"),
            HttpStatus.FORBIDDEN);
      }
      User user = opUser.get();
      user.setVerificationToken(null);
      user.setVerified(true);
      user.setRole(List.of("USER"));
      return new ResponseEntity<>(
          new ApiResponse<>("Email verified successfully"), HttpStatus.OK);
    } catch (Exception e) {
      log.error("Error in verification {}", e);
      return new ResponseEntity<>(new ApiResponse<>(ERROR_STRING + e),
                                  HttpStatus.UNAUTHORIZED);
    }
  }
}
