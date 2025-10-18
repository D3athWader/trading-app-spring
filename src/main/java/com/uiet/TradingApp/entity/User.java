package com.uiet.TradingApp.entity;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  private String userName;
  private String password;
  private String email;
  @ElementCollection(fetch = FetchType.EAGER) private List<String> role;
  private LocalDateTime createadAt;
  private LocalDateTime lastActive;
  private BigDecimal balance;
  private String country;
  private String status; // Account is active , not logged in , suspended
}
