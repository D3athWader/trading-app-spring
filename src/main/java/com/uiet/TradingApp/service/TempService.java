package com.uiet.TradingApp.service;

import com.uiet.TradingApp.entity.Temp;
import com.uiet.TradingApp.repository.TempRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TempService {
  private final TempRepository tempRepository;

  @Transactional
  public void newEntry(String jwtToken) {
    log.info("New JWT Entry {}", jwtToken);
    tempRepository.deleteExpired(LocalDateTime.now().minusMinutes(10));
    Temp tempJwtStorage = Temp.builder().jwtToken(jwtToken).build();
    tempRepository.save(tempJwtStorage);
  }

  @Transactional
  public void deleteEntry(String jwtToken) {
    log.info("Deleting JWT Entry {}", jwtToken);
    tempRepository.deleteById(jwtToken);
  }

  @Transactional
  public boolean checkEntry(String jwtToken) {
    return tempRepository.existsById(jwtToken);
  }
}
