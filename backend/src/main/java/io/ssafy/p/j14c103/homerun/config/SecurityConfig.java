package io.ssafy.p.j14c103.homerun.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Value("${spring.h2.console.enabled:false}")
    private boolean h2ConsoleEnabled;

    @Value("${app.docs.enabled:false}")
    private boolean docsEnabled;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RequestIdFilter requestIdFilter,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AuthenticationEntryPoint jwtAuthenticationEntryPoint
    ) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
        );
        http.authorizeHttpRequests(authorize -> {
            authorize.requestMatchers("/api/auth/**").permitAll();
            authorize.requestMatchers(
                    "/actuator/health",
                    "/actuator/health/**",
                    "/actuator/prometheus"
            ).permitAll();
            if (docsEnabled) {
                authorize.requestMatchers("/docs", "/docs/**").permitAll();
            }
            if (h2ConsoleEnabled) {
                authorize.requestMatchers("/h2-console/**").permitAll();
            }
            authorize.requestMatchers("/api/**").authenticated();
            authorize.anyRequest().denyAll();
        });
        if (h2ConsoleEnabled) {
            http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));
        }
        http.addFilterBefore(
                requestIdFilter,
                UsernamePasswordAuthenticationFilter.class
        );
        http.addFilterAfter(
                jwtAuthenticationFilter,
                RequestIdFilter.class
        );

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider
    ) {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }

    @Bean
    public RequestIdFilter requestIdFilter() {
        return new RequestIdFilter();
    }

    @Bean
    public AuthenticationEntryPoint jwtAuthenticationEntryPoint(
            ObjectMapper objectMapper
    ) {
        return new JwtAuthenticationEntryPoint(objectMapper);
    }
}
