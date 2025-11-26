// package com.uiet.TradingApp.config;
//
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.messaging.Message;
// import org.springframework.security.authorization.AuthorizationManager;
// import
// org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
// import
// org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
// import
// org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
//
// @Configuration
// @EnableWebSocketSecurity
// @EnableMethodSecurity
// public class WebSocketSecurityConfig {
//
// @Bean
// AuthorizationManager<Message<?>> messageAuthorizationManager(
// MessageMatcherDelegatingAuthorizationManager.Builder messages) {
//
// messages.nullDestMatcher()
// .permitAll()
// .simpSubscribeDestMatchers("/topic/**")
// .permitAll()
// .simpDestMatchers("/app/**")
// .permitAll()
// .anyMessage()
// .permitAll();
//
// return messages.build();
// }
// }
