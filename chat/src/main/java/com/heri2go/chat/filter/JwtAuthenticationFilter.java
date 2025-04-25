package com.heri2go.chat.filter;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.heri2go.chat.web.service.auth.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter implements WebFilter {

        private final JwtService jwtService;
        private final ReactiveUserDetailsService userDetailsService;

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
                return Mono.just(exchange)
                                .filter(this::checkExceptionPath)
                                .flatMap(this::extractToken)
                                .flatMap(token -> authenticateRequest(exchange, chain, token))
                                .switchIfEmpty(chain.filter(exchange));
        }

        private boolean checkExceptionPath(ServerWebExchange exchange) {
                String path = exchange.getRequest().getURI().getPath();
                return !path.startsWith("/js/") &&
                                !path.startsWith("/css/") &&
                                !path.startsWith("/img/") &&
                                !path.startsWith("/api/auth/") &&
                                !path.startsWith("/login") &&
                                !path.startsWith("/register") &&
                                !path.startsWith("/favicon.ico") &&
                                !path.startsWith("/ws");
        }

        private Mono<String> extractToken(ServerWebExchange exchange) {
                String token = exchange.getRequest().getHeaders().getFirst("Authorization");
                log.info("HTTP request, token : {} / url : {}", token, exchange.getRequest().getURI());
                return Mono.justOrEmpty(token)
                                .filter(header -> header.startsWith("Bearer "))
                                .map(header -> header.substring(7));
        }

        private Mono<Void> authenticateRequest(ServerWebExchange exchange, WebFilterChain chain, String token) {
                return Mono.justOrEmpty(jwtService.extractUsername(token))
                                .flatMap(username -> userDetailsService.findByUsername(username)
                                .filter(userDetails -> jwtService.validateToken(token, username))
                                .flatMap(userDetails -> {
                                                Authentication authentication =
                                                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                                                return chain.filter(exchange)
                                                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                                                }))
                                .switchIfEmpty(chain.filter(exchange));
        }
}