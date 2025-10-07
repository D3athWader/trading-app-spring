package com.uiet.TradingApp.controller;

import com.uiet.TradingApp.DTO.UserDTO;
import com.uiet.TradingApp.entity.User;
import com.uiet.TradingApp.repository.UserRepository;
import com.uiet.TradingApp.service.UserService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("user-panel")
public class UserController {

  @GetMapping("/hello")
  public String helloController() {
    return new String("Hello Controller");
  }

  @Autowired private UserService userService;
  @Autowired private UserRepository userRepository;

  @PostMapping("/create-user")
  public ResponseEntity<User> createUser(@RequestBody User user) {
    userService.createUser(user);
    return new ResponseEntity<>(user, HttpStatus.CREATED);
  }

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

  @DeleteMapping("/delete/{userName}")
  public ResponseEntity<?> deleteUser(@PathVariable String userName) {
    Optional<User> user = userRepository.findByUserName(userName);
    if (!user.isPresent()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      userRepository.delete(user.get());
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
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
}
