package com.uiet.TradingApp.AdminController;

import com.uiet.TradingApp.entity.User;
import com.uiet.TradingApp.repository.UserRepository;
import com.uiet.TradingApp.service.UserService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminUserController {

  @Autowired
  private UserService userService;
  @Autowired
  private UserRepository userRepository;

  @GetMapping("/make-admin/{username}")
  public ResponseEntity<?> makeUserAdmin(@PathVariable String username) {
    try {
      User user = userRepository.findByUserName(username).orElseThrow(
          () -> new Exception("Username not found"));
      List<String> roles = user.getRole();
      roles.add("ADMIN");
      user.setRole(roles);
      userService.saveUser(user);
      return new ResponseEntity<>("User " + username + " is now admin",
          HttpStatus.CREATED);
    } catch (Exception e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
  }
}
