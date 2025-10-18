package com.uiet.TradingApp.service;

import com.uiet.TradingApp.entity.Company;
import com.uiet.TradingApp.repository.CompanyRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CompanyService {

  @Autowired CompanyRepository companyRepository;
  @Autowired StockService stockService;

  public List<Company> getAllCompanies() { return companyRepository.findAll(); }

  public Optional<Company> getByTickerSymbol(String tickerSymbol) {
    return companyRepository.findByTickerSymbol(tickerSymbol);
  }

  public Optional<Company> getByName(String name) {
    return companyRepository.findByName(name);
  }

  public List<Company> getBySector(String sector) {
    return companyRepository.findBySector(sector);
  }

  public List<Company> getAll() { return companyRepository.findAll(); }

  // marketCap should be calculated correctly
  @Transactional
  public void newEntry(Company company) {
    log.info("INFO: Creating new company {}", company.getName());
    company.setMarketCap(0.00);
    company.setCreatedAt(LocalDateTime.now());
    companyRepository.save(company);
  }

  public void saveEntry(Company company) { companyRepository.save(company); }

  @Transactional
  public void deleteEntry(Company company) {
    log.info("INFO: Deleting company {}", company.getName());
    companyRepository.delete(company);
  }

  public Optional<Company> findById(Long id) {
    return companyRepository.findById(id);
  }
}
