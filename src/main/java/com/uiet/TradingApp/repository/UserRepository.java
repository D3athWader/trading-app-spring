package com.uiet.TradingApp.repository;

import com.uiet.TradingApp.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUserName(String userName);
}
