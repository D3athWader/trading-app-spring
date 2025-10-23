package com.uiet.TradingApp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

@Configuration
@EnableWebSocketSecurity
@EnableMethodSecurity
public class WebSocketSecurityConfig {

  @Bean
  AuthorizationManager<Message<?>> messageAuthorizationManager(
      MessageMatcherDelegatingAuthorizationManager.Builder messages) {

    messages.simpDestMatchers("/app/admin/**")
        .hasRole("ADMIN")
        .simpDestMatchers("/app/user/**")
        .hasRole("USER")
        .simpSubscribeDestMatchers("/topic/admin/**")
        .hasRole("ADMIN")
        .simpSubscribeDestMatchers("/topic/**")
        .authenticated()
        .anyMessage()
        .authenticated();

    return messages.build();
  }
}
