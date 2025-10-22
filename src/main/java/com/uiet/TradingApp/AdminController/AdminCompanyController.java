package com.uiet.TradingApp.AdminController;

import com.uiet.TradingApp.entity.Company;
import com.uiet.TradingApp.service.CompanyService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/company")
@Slf4j
@RequiredArgsConstructor
public class AdminCompanyController {
  private final CompanyService companyService;

  @PostMapping("/create-company")
  public ResponseEntity<?> createCompany(@RequestBody Company company) {
    companyService.newEntry(company);
    log.info("INFO: Company created successfully {}", company.getName());
    return new ResponseEntity<>(company, HttpStatus.CREATED);
  }

  @DeleteMapping("/delete/{id}")
  public ResponseEntity<?> deleteCompany(@PathVariable Long id) {
    Optional<Company> company = companyService.findById(id);
    if (!company.isPresent()) {
      log.warn("Company with id {} not found", id);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      companyService.deleteEntry(company.get());
      log.info("INFO: Company deleted successfully {}",
               company.get().getName());
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
  }
}
