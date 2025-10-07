package com.uiet.TradingApp.service;

import com.uiet.TradingApp.entity.Company;
import com.uiet.TradingApp.repository.CompanyRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {

  @Autowired CompanyRepository companyRepository;

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
  // Can be done after craeting Stock Service
  public void newEntry(Company company) {
    company.setMarketCap(0.00);
    company.setCreatedAt(LocalDateTime.now());
    companyRepository.save(company);
  }

  public void saveEntry(Company company) { companyRepository.save(company); }

  public void deleteEntry(Company company) {
    companyRepository.delete(company);
  }

  public Optional<Company> findById(Long id) {
    return companyRepository.findById(id);
  }
}
