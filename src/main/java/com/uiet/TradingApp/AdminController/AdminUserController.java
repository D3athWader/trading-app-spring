package com.uiet.TradingApp.AdminController;

import com.uiet.TradingApp.DTO.ApiResponse;
import com.uiet.TradingApp.entity.User;
import com.uiet.TradingApp.repository.UserRepository;
import com.uiet.TradingApp.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminUserController {

  private final UserService userService;
  private final UserRepository userRepository;

  @GetMapping("/check")
  public ResponseEntity<ApiResponse<Void>> checkAdmin() {
    return new ResponseEntity<>(new ApiResponse<>("You are admin"),
                                HttpStatus.OK);
  }

  @GetMapping("/make-admin/{username}")
  public ResponseEntity<ApiResponse<Void>>
  makeUserAdmin(@PathVariable String username) {
    try {
      User user = userRepository.findByUserName(username).orElseThrow(
          () -> new Exception("Username not found"));
      List<String> roles = user.getRole();
      roles.add("ADMIN");
      user.setRole(roles);
      userService.saveUser(user);
      return new ResponseEntity<>(
          new ApiResponse<>("User " + username + " is now admin"),
          HttpStatus.CREATED);
    } catch (Exception e) {
      return new ResponseEntity<>(new ApiResponse<>(e.getMessage()),
                                  HttpStatus.NOT_FOUND);
    }
  }
}
