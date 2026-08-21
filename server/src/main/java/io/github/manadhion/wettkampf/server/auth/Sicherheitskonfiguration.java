package io.github.manadhion.wettkampf.server.auth;

import java.io.IOException;
import java.time.Instant;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class Sicherheitskonfiguration {

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    FilterRegistrationBean<SitzungFilter> sitzungFilterRegistrierung(SitzungFilter sitzungFilter) {
        FilterRegistrationBean<SitzungFilter> registrierung =
                new FilterRegistrationBean<>(sitzungFilter);
        registrierung.setEnabled(false);
        return registrierung;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SitzungFilter sitzungFilter)
            throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sitzung -> sitzung
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(fehler -> fehler
                        .authenticationEntryPoint((request, response, exception) ->
                                nichtAngemeldet(response)))
                .authorizeHttpRequests(anfragen -> anfragen
                        .requestMatchers("/error", "/api/v1/status", "/api/v1/anmeldung").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(sitzungFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private void nichtAngemeldet(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("""
                {"code":"NICHT_ANGEMELDET","nachricht":"Eine gültige Anmeldung ist erforderlich.","zeitpunkt":"%s"}
                """.formatted(Instant.now()));
    }
}
