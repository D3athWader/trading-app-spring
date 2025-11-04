package com.uiet.TradingApp.entity;

import jakarta.persistence.Column;
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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(unique = true, nullable = false) private String userName;
  @Column(nullable = false) private String password;
  @Column(unique = true, nullable = false) private String email;
  @ElementCollection(fetch = FetchType.EAGER) private List<String> role;
  private LocalDateTime createadAt;
  private LocalDateTime lastActive;
  private BigDecimal balance;
  private String country;
  private String status; // Account is active , not logged in , suspended
  // EMAIL VERIFICATION
  private String verificationToken;
  @Column(nullable = false) private boolean isVerified;
  private String resetToken;
  // TOTP
  @Column(nullable = false, columnDefinition = "boolean default false")
  private boolean isTotpEnabled;
  private String secret;
}
