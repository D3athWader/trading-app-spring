package com.uiet.TradingApp.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
  private String SECRET_KEY =
      "b116e8f70c8bf981fde822195a3ca6c44faa7108b4c5d43c";

  public String generateToken(String userName) {
    Map<String, Object> claims = new HashMap<>();
    return createToken(claims, userName);
  }

  private String createToken(Map<String, Object> claims, String subject) {
    return Jwts.builder()
        .claims(claims)
        .subject(subject)
        .header()
        .empty()
        .add("typ", "JWT")
        .and()
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + 100 * 60 * 5))
        .signWith(getSigningKey())
        .compact();
  }

  private SecretKey getSigningKey() {

    return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
  }
}
