package com.uiet.TradingApp.service;

import com.uiet.TradingApp.entity.User;
import com.uiet.TradingApp.repository.UserRepository;
import java.math.BigDecimal;
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
    user.setBalance(BigDecimal.ZERO);
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

  public void deductBalance(User user, BigDecimal removeBalance) {
    BigDecimal userBalance = user.getBalance();
    if (userBalance.compareTo(removeBalance) >= 0) {
      user.setBalance(userBalance.subtract(removeBalance));
    } else {
      throw new RuntimeException("Insufficient balance");
    }
  }

  public void addBalance(User user, BigDecimal addBalance) {
    BigDecimal userBalance = user.getBalance();
    user.setBalance(userBalance.add(addBalance));
  }

  public Long getUserId(User user) { return user.getId(); }

  public BigDecimal getUserBalance(User user) { return user.getBalance(); }

  public void deleteUser(User user) { userRepository.delete(user); }

  public List<User> getAll() { return userRepository.findAll(); }
}
