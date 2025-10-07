package com.uiet.TradingApp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  private String userName;
  private String password;
  private String email;
  private String role;
  private LocalDateTime createadAt;
  private LocalDateTime lastActive;
  private Double balance;
  private String country;
  private String status; // Account is active , not logged in , suspended
}
