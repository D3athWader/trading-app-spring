package com.uiet.TradingApp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Entity
@Table(name = "temp_jwt")
@Builder
public class Temp {
  @Id private String jwtToken;
  @Column(nullable = false) private LocalDateTime timeCreated;

  @PrePersist
  public void addTime() {
    this.timeCreated = LocalDateTime.now();
  }
}
