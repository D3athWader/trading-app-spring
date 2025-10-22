package com.uiet.TradingApp.controller;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("user-panel")
@RequiredArgsConstructor
public class UserController {

  @GetMapping("/hello")
  public String helloController() {
    return new String("Hello Controller");
  }

  private final UserService userService;
  private final UserRepository userRepository;
  private final JwtUtil jwtUtil;
  private final TempService tempService;

  @GetMapping("/find-user/{userName}")
  public ResponseEntity<?> findUser(@PathVariable String userName) {
    Optional<User> user = userRepository.findByUserName(userName);
    if (user.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      User localUser = user.get();
      UserDTO sendUser =
          new UserDTO(localUser.getId(), userName, localUser.getCountry());
      return new ResponseEntity<>(sendUser, HttpStatus.OK);
    }
  }

  @DeleteMapping("/delete")
  public ResponseEntity<?>
  deleteUser(@RequestHeader("Authorization") String authHeader) {
    try {
      String jwtToken = authHeader.substring(7);
      String userName = jwtUtil.extractUsername(jwtToken);
      userService.deleteUser(userName);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (Exception e) {
      return new ResponseEntity<>("Exception " + e, HttpStatus.FORBIDDEN);
    }
  }

  @GetMapping("/all-users")
  public ResponseEntity<?> getAllUsers() {
    List<User> userList = userService.getAll();
    if (userList.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(userList, HttpStatus.OK);
    }
  }

  @GetMapping("/add-balance")
  public ResponseEntity<?> addBalance(@RequestHeader("Authorization")
                                      String authHeader, BigDecimal balance) {
    // Just using a placeholder for now
    try {
      String token = authHeader.substring(7);
      String userName = jwtUtil.extractUsername(token);
      User user = userRepository.findByUserName(userName).orElseThrow(
          () -> new RuntimeException("User not found"));
      userService.addBalance(user, balance);
      log.info("INFO: Adding balance {} for user {}", balance, userName);
      return new ResponseEntity<>("Balance added", HttpStatus.CREATED);
    } catch (Exception e) {
      log.error("ERROR: Failed to add balance for user");
      return new ResponseEntity<>("Exception " + e, HttpStatus.FORBIDDEN);
    }
  }

  @GetMapping("/logout")
  public ResponseEntity<?>
  logout(@RequestHeader("Authorization") String authHeader) {
    try {
      String token = authHeader.substring(7);
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
