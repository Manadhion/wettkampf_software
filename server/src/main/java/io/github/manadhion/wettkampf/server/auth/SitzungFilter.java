package io.github.manadhion.wettkampf.server.auth;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class SitzungFilter extends OncePerRequestFilter {

    private final AuthService authService;

    public SitzungFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        BearerToken.ausAuthorizationOptional(request.getHeader("Authorization"))
                .filter(authService::istTokenGueltig)
                .ifPresent(token -> SecurityContextHolder.getContext().setAuthentication(
                        UsernamePasswordAuthenticationToken.authenticated(
                                "vereinskonto", token, List.of())));
        filterChain.doFilter(request, response);
    }
}
