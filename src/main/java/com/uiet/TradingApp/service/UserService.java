package com.uiet.TradingApp.service;

import com.uiet.TradingApp.entity.User;
import com.uiet.TradingApp.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {

  @Autowired private UserRepository userRepository;

  private static final PasswordEncoder PASSWORD_ENCODER =
      new BCryptPasswordEncoder();

  @Transactional
  public void saveUser(User user) {
    log.info("INFO: Updating / Saving user {}", user.getUserName());
    userRepository.save(user);
  }

  @Transactional
  public void createUser(User user) {
    user.setPassword(PASSWORD_ENCODER.encode(user.getPassword()));
    user.setCreateadAt(LocalDateTime.now());
    user.setBalance(BigDecimal.ZERO);
    // user.getRole().add("USER");
    user.setRole(List.of("USER"));
    user.setLastActive(LocalDateTime.now());
    user.setStatus("Active");
    log.info("INFO: Creating user {}", user.getUserName());
    userRepository.save(user);
  }

  @Transactional
  public void updateUser(User user) {
    user.setPassword(PASSWORD_ENCODER.encode(user.getPassword()));
    user.setLastActive(LocalDateTime.now());
    userRepository.save(user);
  }

  @Transactional
  public void deductBalance(User user, BigDecimal removeBalance) {
    BigDecimal userBalance = user.getBalance();
    if (userBalance.compareTo(removeBalance) >= 0) {
      log.info("INFO: Deducting {} from {}", removeBalance, user.getUserName());
      user.setBalance(userBalance.subtract(removeBalance));
      userRepository.save(user);
    } else {
      log.error("ERROR: Insufficient balance of {}", user.getUserName());
      throw new RuntimeException("Insufficient balance");
    }
  }

  @Transactional
  public void addBalance(User user, BigDecimal addBalance) {
    BigDecimal userBalance = user.getBalance();
    user.setBalance(userBalance.add(addBalance));
    log.info("INFO: Adding {} to {}", addBalance, user.getUserName());
    userRepository.save(user);
  }

  public Long getUserId(User user) {
    log.info("INFO: Getting user id for {}", user.getUserName());
    return user.getId();
  }

  public BigDecimal getUserBalance(User user) {
    log.info("INFO: Getting user balance for {}", user.getUserName());
    return user.getBalance();
  }

  @Transactional
  public void deleteUser(String username) {
    log.info("INFO: Deleting user {}", username);
    userRepository.deleteByUserName(username);
  }

  public void deleteUserById(Long Id) {
    log.info("INFO: Deleting user by id {}", Id);
    userRepository.deleteById(Id);
  }

  public List<User> getAll() {
    log.info("INFO: Getting all users");
    return userRepository.findAll();
  }
}
