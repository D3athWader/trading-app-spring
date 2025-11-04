package com.uiet.TradingApp.service;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

import com.uiet.TradingApp.entity.User;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class TotpService {
  private final SecretGenerator secretGenerator =
      new DefaultSecretGenerator(64);
  private final UserService userService;
  private final CodeGenerator codeGenerator =
      new DefaultCodeGenerator(HashingAlgorithm.SHA512);
  private final CodeVerifier verifier =
      new DefaultCodeVerifier(codeGenerator, new SystemTimeProvider());

  private String generateSecret() { return secretGenerator.generate(); }

  @Transactional
  public String getSecret(String username) {
    User user = userService.getUserByUsername(username).orElseThrow(
        () -> new RuntimeException("User not found"));
    String secret = generateSecret();
    user.setSecret(secret);
    userService.saveUser(user);
    return secret;
  }

  @Transactional
  public boolean enableTotp(Long userId, String code) {
    User user = userService.getUserById(userId).orElseThrow(
        () -> new RuntimeException("User not found"));
    String secret = user.getSecret();
    if (verifier.isValidCode(secret, code)) {
      user.setTotpEnabled(true);
      userService.saveUser(user);
      return true;
    }
    return false;
  }

  public String generateQrCode(String username, String secret)
      throws QrGenerationException {
    QrData data = new QrData.Builder()
                      .label("TradingApp: " + username)
                      .secret(secret)
                      .issuer("TradingApp")
                      .algorithm(HashingAlgorithm.SHA512)
                      .digits(6)
                      .period(30)
                      .build();
    try {
      QrGenerator generator = new ZxingPngQrGenerator();
      byte[] imageData = generator.generate(data);
      String mimeType = generator.getImageMimeType();
      return getDataUriForImage(imageData, mimeType);
    } catch (Exception e) {
      log.error("Error generating QR code: {}", e.getMessage());
      throw new QrGenerationException("Error generating QR code", e);
    }
  }

  public boolean verifyToken(Long userId, String code) {
    User user = userService.getUserById(userId).orElseThrow(
        () -> new RuntimeException("User Not found"));
    String secret = user.getSecret();
    return verifier.isValidCode(secret, code);
  }
}
