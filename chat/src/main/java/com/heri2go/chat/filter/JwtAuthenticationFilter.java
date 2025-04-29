package com.heri2go.chat.filter;

import com.heri2go.chat.web.exception.InvalidJwtTokenException;
import com.heri2go.chat.web.service.auth.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtService jwtService;
    private final ReactiveUserDetailsService userDetailsService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return Mono.defer(() -> {
            if (checkExceptionPath(exchange)) {
                return chain.filter(exchange);
            }
            return extractToken(exchange)
                    .flatMap(token -> {
                        String username = jwtService.extractUsername(token);
                        return authenticateToken(exchange, chain, token, username);
                    })
                    .onErrorResume(InvalidJwtTokenException.class, e -> {
                        log.error("Authentication failed: {}", e.getMessage());
                        return chain.filter(exchange);
                    });
        });
    }

    private boolean checkExceptionPath(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        return path.startsWith("/js/") ||
                path.startsWith("/css/") ||
                path.startsWith("/img/") ||
                path.startsWith("/api/auth/") ||
                path.startsWith("/login") ||
                path.startsWith("/register") ||
                path.startsWith("/favicon.ico");
    }

    private Mono<String> extractToken(ServerWebExchange exchange) {
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        return Mono.justOrEmpty(token)
                .filter(header -> header.startsWith("Bearer "))
                .map(header -> header.substring(7))
                .switchIfEmpty(Mono.error(new InvalidJwtTokenException("Invalid token")));
    }

    private Mono<Void> authenticateToken(ServerWebExchange exchange, WebFilterChain chain, String token, String username) {
        return userDetailsService.findByUsername(username)
                .filter(userDetails -> jwtService.validateToken(token, username))
                .switchIfEmpty(Mono.error(new InvalidJwtTokenException("Invalid token")))
                .map(userDetails -> new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                ))
                .flatMap(authentication -> chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
                );
    }
}