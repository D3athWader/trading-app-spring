package com.uiet.TradingApp.controller;

import com.uiet.TradingApp.DTO.CompanyDTO;
import com.uiet.TradingApp.entity.Company;
import com.uiet.TradingApp.service.CompanyService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/company")
public class CompanyController {

  @Autowired
  CompanyService companyService;

  @GetMapping("/all")
  public ResponseEntity<?> getAllCompanies() {
    List<Company> companies = companyService.getAllCompanies();
    if (companies.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      List<CompanyDTO> companiesDTO = companies.stream().map(this::convertToDTO).toList();
      return new ResponseEntity<>(companiesDTO, HttpStatus.OK);
    }
  }

  @GetMapping("/id/{id}")
  public ResponseEntity<?> getCompanyById(@PathVariable Long id) {
    Optional<Company> company = companyService.findById(id);
    if (company.isPresent()) {
      return new ResponseEntity<>(convertToDTO(company.get()), HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping("/name/{companyName}")
  public ResponseEntity<?> findCompanyByName(@PathVariable String companyName) {
    Optional<Company> company = companyService.getByName(companyName);
    if (company.isPresent()) {
      return new ResponseEntity<>(convertToDTO(company.get()), HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  private CompanyDTO convertToDTO(Company company) {
    CompanyDTO dto = new CompanyDTO();
    dto.setName(company.getName());
    dto.setTickerSymbol(company.getTickerSymbol());
    dto.setSector(company.getSector());
    dto.setMarketCap(company.getMarketCap());
    // don't include sensitive info like shares or createdAt
    return dto;
  }
}
