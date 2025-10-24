package com.uiet.TradingApp.config;

import com.uiet.TradingApp.filter.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SpringSecurity {

  private final JwtFilter jwtFilter;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity httpSecurity)
      throws Exception {
    String isAdmin = "ROLE_ADMIN";
    String isUser = "ROLE_USER";
    String isCompany = "ROLE_COMPANY";
    httpSecurity.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(
            auth
            -> auth.requestMatchers("/admin/**")
                   .hasAuthority(isAdmin)
                   .requestMatchers("/user-panel/**")
                   .hasAuthority(isUser)
                   .requestMatchers("/company")
                   .hasAuthority(isCompany)
                   .requestMatchers("/company/**")
                   .hasAnyAuthority(isCompany, isAdmin, isUser)
                   .requestMatchers("/order/**")
                   .hasAnyAuthority(isUser, isCompany, isAdmin)
                   .requestMatchers("/stock/**")
                   .hasAnyAuthority(isUser, isCompany, isAdmin)
                   .requestMatchers("/public/**")
                   .permitAll()
                   .requestMatchers("/ws/**")
                   .hasAnyAuthority(isUser, isAdmin, isCompany)
                   .requestMatchers("/portfolio/**")
                   .hasAnyAuthority(isUser, isCompany, isAdmin)
                   .requestMatchers("/portfolio/new-portfolio")
                   .hasAnyAuthority(isAdmin, isCompany)
                   .anyRequest()
                   .permitAll());
    httpSecurity.sessionManagement(
        session
        -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    httpSecurity.addFilterBefore(jwtFilter,
                                 UsernamePasswordAuthenticationFilter.class);

    return httpSecurity.build();
  }

  @Bean
  public AuthenticationManager

  authenticationManager(AuthenticationConfiguration authenticationConfiguration)
      throws Exception {

    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
