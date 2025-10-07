package com.uiet.TradingApp.service;

import com.uiet.TradingApp.entity.User;
import com.uiet.TradingApp.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  @Autowired private UserRepository userRepository;

  private static final PasswordEncoder PASSWORD_ENCODER =
      new BCryptPasswordEncoder();

  public void saveUser(User user) { userRepository.save(user); }

  public void createUser(User user) {
    user.setPassword(PASSWORD_ENCODER.encode(user.getPassword()));
    user.setCreateadAt(LocalDateTime.now());
    user.setBalance(0.00);
    user.setRole("USER");
    user.setLastActive(LocalDateTime.now());
    user.setStatus("Active");
    userRepository.save(user);
  }

  public void updateUser(User user) {
    user.setPassword(PASSWORD_ENCODER.encode(user.getPassword()));
    user.setLastActive(LocalDateTime.now());
    userRepository.save(user);
  }

  public void deleteUser(User user) { userRepository.delete(user); }

  public List<User> getAll() { return userRepository.findAll(); }
}
