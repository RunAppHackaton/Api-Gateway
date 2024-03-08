package com.runapp.apigateway.utill;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Base64;

@Component
public class JwtTokenExtractorGlobalFilter implements GlobalFilter, Ordered {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String X_USER_ID_HEADER = "X-UserId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        String authorizationHeader = headers.getFirst(AUTHORIZATION_HEADER);

        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            String jwtToken = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
            String sub = extractSubFromToken(jwtToken);

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header(X_USER_ID_HEADER, sub)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        }

        return chain.filter(exchange);
    }

    private String extractSubFromToken(String jwtToken) {
        String[] jwtParts = jwtToken.split("\\.");
        if (jwtParts.length >= 2) {
            String base64EncodedPayload = jwtParts[1];
            byte[] decodedPayload = Base64.getDecoder().decode(base64EncodedPayload);
            String payloadJson = new String(decodedPayload);
            return extractSubClaimFromPayload(payloadJson);
        }
        return null;
    }

    private String extractSubClaimFromPayload(String payloadJson) {
        try {
            // Parse the JSON payload using Jackson ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode payloadNode = objectMapper.readTree(payloadJson);

            // Extract the value of the "sub" claim
            JsonNode subNode = payloadNode.get("sub");
            if (subNode != null && subNode.isTextual()) {
                return subNode.asText();
            }
        } catch (IOException e) {
            // Handle JSON parsing exception
            e.printStackTrace();
        }

        return null; // Return null if "sub" claim is not found or cannot be extracted
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
