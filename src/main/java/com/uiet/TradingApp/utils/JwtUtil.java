// TODO: Implement Refreshing Tokens
package com.uiet.TradingApp.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
  @Value("${jwt.secret}") private String secretKey;

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
        .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 10))
        .signWith(getSigningKey())
        .compact();
  }

  public String generateEmailVerificationToken(String userName) {
    Map<String, Object> claims = new HashMap<>();
    return createEmailToken(claims, userName);
  }

  private String createEmailToken(Map<String, Object> claims, String subject) {
    return Jwts.builder()
        .claims(claims)
        .subject(subject)
        .header()
        .empty()
        .add("typ", "JWT")
        .and()
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
        .signWith(getSigningKey())
        .compact();
  }

  private SecretKey getSigningKey() {

    return Keys.hmacShaKeyFor(secretKey.getBytes());
  }

  public String extractUsername(String token) {
    return extractAllClaims(token).getSubject();
  }

  public Boolean validateToken(String token) { return !isTokenExpired(token); }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  public Date extractExpiration(String token) {
    return extractAllClaims(token).getExpiration();
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
