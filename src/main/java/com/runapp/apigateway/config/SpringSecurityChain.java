package com.runapp.apigateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runapp.apigateway.exception.ServerAuthFailHandlerImpl;
import jakarta.inject.Qualifier;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@AllArgsConstructor
public class SpringSecurityChain {

    private final ObjectMapper objectMapper;

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) throws Exception {
        http
                .authorizeExchange()
                .anyExchange().authenticated()
                .and()
                .oauth2ResourceServer()
                .jwt()
                .and()
                .authenticationFailureHandler(new ServerAuthFailHandlerImpl(objectMapper));
        return http.build();
    }
}
