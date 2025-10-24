package com.uiet.TradingApp.controller;

import com.uiet.TradingApp.DTO.ApiResponse;
import com.uiet.TradingApp.DTO.CompanyDTO;
import com.uiet.TradingApp.entity.Company;
import com.uiet.TradingApp.service.CompanyService;
import com.uiet.TradingApp.service.UserService;
import com.uiet.TradingApp.utils.JwtUtil;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
public class CompanyController {

  private final CompanyService companyService;
  private final JwtUtil jwtUtil;
  private final UserService userService;

  @GetMapping("/all")
  public ResponseEntity<ApiResponse<List<CompanyDTO>>> getAllCompanies() {
    List<Company> companies = companyService.getAllCompanies();
    if (companies.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      List<CompanyDTO> companiesDTO =
          companies.stream().map(this::convertToDTO).toList();
      return new ResponseEntity<>(new ApiResponse<>(companiesDTO),
                                  HttpStatus.OK);
    }
  }

  @GetMapping("/id/{id}")
  public ResponseEntity<ApiResponse<CompanyDTO>>
  getCompanyById(@PathVariable Long id) {
    Optional<Company> company = companyService.findById(id);
    if (company.isPresent()) {
      return new ResponseEntity<>(
          new ApiResponse<>(convertToDTO(company.get())), HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping("/name/{companyName}")
  public ResponseEntity<ApiResponse<CompanyDTO>>
  findCompanyByName(@PathVariable String companyName) {
    Optional<Company> company = companyService.getByName(companyName);
    if (company.isPresent()) {
      return new ResponseEntity<>(
          new ApiResponse<>(convertToDTO(company.get())), HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping
  public ResponseEntity<ApiResponse<Company>>
  getInfoAboutOwn(Company company,
                  @RequestHeader("Authorization") String authHeader) {
    String username = jwtUtil.extractUsername(authHeader);
    List<String> roles = userService.getRolesByUsername(username);
    if (!roles.contains("COMPANY")) {
      return new ResponseEntity<>(new ApiResponse<>("You are not a company"),
                                  HttpStatus.FORBIDDEN);
    }
    return new ResponseEntity<>(new ApiResponse<>(company), HttpStatus.OK);
  }

  private CompanyDTO convertToDTO(Company company) {
    CompanyDTO dto = new CompanyDTO();
    dto.setName(company.getName());
    dto.setTickerSymbol(company.getTickerSymbol());
    dto.setSector(company.getSector());
    dto.setMarketCap(company.getMarketCap());
    return dto;
  }
}
