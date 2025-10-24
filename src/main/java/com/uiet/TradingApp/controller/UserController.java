package com.uiet.TradingApp.controller;

import com.uiet.TradingApp.DTO.ApiResponse;
import com.uiet.TradingApp.DTO.UserDTO;
import com.uiet.TradingApp.entity.User;
import com.uiet.TradingApp.repository.UserRepository;
import com.uiet.TradingApp.service.TempService;
import com.uiet.TradingApp.service.UserService;
import com.uiet.TradingApp.utils.JwtUtil;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("user-panel")
@RequiredArgsConstructor
public class UserController {

  @GetMapping("/hello")
  public ResponseEntity<ApiResponse<Void>> helloController() {
    return new ResponseEntity<>(new ApiResponse<>("Hello"), HttpStatus.OK);
  }

  private final UserService userService;
  private final UserRepository userRepository;
  private final JwtUtil jwtUtil;
  private final TempService tempService;

  @GetMapping("/find-user/{userName}")
  public ResponseEntity<ApiResponse<UserDTO>>
  findUser(@PathVariable String userName) {
    Optional<User> user = userRepository.findByUserName(userName);
    if (user.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      User localUser = user.get();
      UserDTO sendUser =
          new UserDTO(localUser.getId(), userName, localUser.getCountry());
      return new ResponseEntity<>(new ApiResponse<>(sendUser), HttpStatus.OK);
    }
  }

  @DeleteMapping("/delete")
  public ResponseEntity<ApiResponse<Void>>
  deleteUser(@RequestHeader("Authorization") String authHeader) {
    try {
      String userName = jwtUtil.extractUsername(authHeader);
      userService.deleteUser(userName);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (Exception e) {
      return new ResponseEntity<>(new ApiResponse<>("Error: " + e),
                                  HttpStatus.FORBIDDEN);
    }
  }

  @GetMapping("/all-users")
  public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
    List<User> userList = userService.getAll();
    if (userList.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(new ApiResponse<>(userList), HttpStatus.OK);
    }
  }

  @GetMapping("/add-balance")
  public ResponseEntity<ApiResponse<Void>>
  addBalance(@RequestHeader("Authorization") String authHeader,
             @RequestParam("balance") BigDecimal balance) {
    // Just using a placeholder for now
    try {
      authHeader = authHeader.substring(7);
      String userName = jwtUtil.extractUsername(authHeader);
      User user = userRepository.findByUserName(userName).orElseThrow(
          () -> new RuntimeException("User not found"));
      userService.addBalance(user, balance);
      log.info("INFO: Adding balance {} for user {}", balance, userName);
      return new ResponseEntity<>(new ApiResponse<>("Balance added"),
                                  HttpStatus.CREATED);
    } catch (Exception e) {
      log.error("ERROR: Failed to add balance for user");
      return new ResponseEntity<>(new ApiResponse<>("Exception " + e),
                                  HttpStatus.FORBIDDEN);
    }
  }

  @GetMapping("/get-balance")
  public ResponseEntity<ApiResponse<BigDecimal>>
  getBalance(@RequestHeader("Authorization") String authHeader) {
    authHeader = authHeader.substring(7);
    String username = jwtUtil.extractUsername(authHeader);
    return new ResponseEntity<>(
        new ApiResponse<>(userService.getUserBalance(username)), HttpStatus.OK);
  }

  @GetMapping("/logout")
  public ResponseEntity<ApiResponse<Void>>
  logout(@RequestHeader("Authorization") String authHeader) {
    try {
      tempService.newEntry(authHeader);
      log.info("logged out {}", authHeader);
      return ResponseEntity.status(HttpStatus.OK)
          .body(new ApiResponse<>("Logout successful"));
    } catch (Exception e) {
      log.error("Error in logout {}", e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new ApiResponse<>("Error in logout"));
    }
  }
}
