package com.uiet.TradingApp.service;

import com.uiet.TradingApp.DTO.NewPortfolio;
import com.uiet.TradingApp.entity.Company;
import com.uiet.TradingApp.entity.Portfolio;
import com.uiet.TradingApp.entity.Stock;
import com.uiet.TradingApp.entity.User;
import com.uiet.TradingApp.repository.CompanyRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {

  private final UserService userService;
  private final CompanyRepository companyRepository;
  private final PortfolioService portfolioService;

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

  // DONE: marketCap should be calculated correctly
  @Transactional
  public void newEntry(Company company) {
    log.info("INFO: Creating new company {}", company.getName());
    company.setMarketCap(BigDecimal.ZERO);
    company.setCreatedAt(LocalDateTime.now());
    companyRepository.save(company);
    newUserForCompany(company);
  }

  @Transactional
  public void saveEntry(Company company) {
    company.setMarketCap(calculateMarketCap(company));
    companyRepository.save(company);
  }

  @Transactional
  public void deleteEntry(Company company) {
    log.info("INFO: Deleting company {}", company.getName());
    companyRepository.delete(company);
  }

  public Optional<Company> findById(Long id) {
    return companyRepository.findById(id);
  }

  private BigDecimal calculateMarketCap(Company company) {
    BigDecimal result = BigDecimal.ZERO;
    for (Stock stock : company.getStocks()) {
      result = result.add(stock.getTotalPrice());
    }
    return result;
  }

  private void newUserForCompany(Company company) {
    User user = new User();
    user.setUserName("user-" + company.getName());
    user.setPassword("password");
    user.setEmail("user-" + company.getName() + "@company.com");
    user.setRole(List.of("ROLE_USER", "ROLE_COMPANY"));
    user.setCreateadAt(LocalDateTime.now());
    user.setCountry("Country");
    user.setStatus("Active");
    user.setVerified(true);
    userService.createUser(user);
    log.info("INFO: Creating user for company {}", company.getName());
  }
}
