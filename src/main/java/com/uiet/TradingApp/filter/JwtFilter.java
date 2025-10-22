package com.uiet.TradingApp.filter;

import com.uiet.TradingApp.service.TempService;
import com.uiet.TradingApp.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
  private final UserDetailsService userDetailsService;
  private final JwtUtil jwtUtil;
  private final TempService tempService;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain)
      throws ServletException, IOException {
    String authorizationHeader = request.getHeader("Authorization");
    String username = null;
    String jwt = null;
    String path = request.getServletPath();
    if (path.startsWith("/public/")) {
      filterChain.doFilter(request, response);
      return;
    }
    if (authorizationHeader == null ||
        !authorizationHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }
    if (authorizationHeader != null &&
        authorizationHeader.startsWith("Bearer ")) {
      try {
        jwt = authorizationHeader.substring(7);
        username = jwtUtil.extractUsername(jwt);
      } catch (Exception e) {
        log.error("Error in JWT filter {}", e);
      }
    }
    if (tempService.checkEntry(jwt)) {
      log.warn("Rejected blacklisted JWT for user: {}", username);
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.getWriter().write(
          "Token has been blacklisted. Please log in again.");
      return;
    }
    if (username != null) {
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);
      boolean validateToken = jwtUtil.validateToken(jwt);
      if (validateToken) {
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        auth.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
      }
    }
    filterChain.doFilter(request, response);
  }
}
