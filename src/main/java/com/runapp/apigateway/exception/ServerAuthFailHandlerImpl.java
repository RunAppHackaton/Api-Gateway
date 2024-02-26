package com.runapp.apigateway.exception;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runapp.apigateway.utill.AccessDeniedEnum;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import com.runapp.apigateway.dto.AccessDeniedResponse;

@Component
@RequiredArgsConstructor
public class ServerAuthFailHandlerImpl implements ServerAuthenticationFailureHandler {

    private final ObjectMapper objectMapper;
    private static final String OPENID_ENDPOINTS = "https://lemur-14.cloud-iam.com/auth/realms/runapp-keycloak/.well-known/openid-configuration";
    private static final Map<String, String> DOCUMENTATION_URLS = Map.of(
            "keycloak main documentation", "https://www.keycloak.org/documentation",
            "oauth2 documentation guide", "https://aaronparecki.com/oauth-2-simplified/#web-server-apps"
    );

    @Override
    public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange, AuthenticationException exception) {
        ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            String errorMessage = objectMapper.writeValueAsString(getAccessDeniedResponse());
            return response.writeWith(Mono.just(response.bufferFactory().wrap(errorMessage.getBytes())))
                    .then(Mono.defer(response::setComplete));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }

    private AccessDeniedResponse getAccessDeniedResponse() {
        return AccessDeniedResponse.builder()
                .error_id(UUID.randomUUID())
                .error_type(AccessDeniedEnum.PERMISSION_DENIED)
                .status_code(HttpStatus.UNAUTHORIZED.value())
                .openid_endpoints(OPENID_ENDPOINTS)
                .documentation_url_list(DOCUMENTATION_URLS)
                .build();
    }
}
