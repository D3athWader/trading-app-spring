package com.uiet.TradingApp.repository;

import com.uiet.TradingApp.entity.Temp;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TempRepository extends JpaRepository<Temp, String> {
  @Modifying
  @Query("DELETE FROM Temp t WHERE t.timeCreated < :cutoff")
  void deleteExpired(@Param("cutoff") LocalDateTime cutoff);
}
