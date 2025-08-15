package com.tuandat.oceanfresh_backend.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static org.springframework.http.HttpMethod.GET;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.tuandat.oceanfresh_backend.filters.JwtTokenFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@EnableWebSecurity(debug = true)
@EnableWebMvc
@RequiredArgsConstructor
public class WebSecurityConfig {
        private final JwtTokenFilter jwtTokenFilter;
        @Value("${api.prefix}")
        private String apiPrefix;

        @Bean
        public SecurityFilterChain webSocketSecurityFilterChain(HttpSecurity http) throws Exception {
                http
                                .securityMatcher("/ws/**") // Chỉ áp dụng cho /ws/**
                                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                                .csrf(AbstractHttpConfigurer::disable);
                return http.build();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                                // .cors(Customizer.withDefaults())
                                .exceptionHandling(customizer -> customizer
                                                .authenticationEntryPoint(
                                                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(requests -> {
                                        requests
                                                        // .requestMatchers("/ws/**").permitAll() // PHẢI ĐỂ TRƯỚC!
                                                        .requestMatchers(
                                                                        String.format("%s/users/register", apiPrefix),
                                                                        String.format("%s/users/login", apiPrefix),
                                                                        // String.format("%s/users/auth/social-login",
                                                                        // apiPrefix),
                                                                        // String.format("%s/users/auth/social/callback",
                                                                        // apiPrefix),
                                                                        // healthcheck
                                                                        String.format("%s/healthcheck/**", apiPrefix),
                                                                        // swagger
                                                                        "/api-docs",
                                                                        "/api-docs/**",
                                                                        "/swagger-resources",
                                                                        "/swagger-resources/**",
                                                                        "/configuration/ui",
                                                                        "/configuration/security",
                                                                        "/swagger-ui/**",
                                                                        "/swagger-ui.html",
                                                                        "/webjars/swagger-ui/**",
                                                                        "/swagger-ui/index.html")
                                                        .permitAll()

                                                        // Public endpoints - GET only
                                                        .requestMatchers(GET, String.format("%s/roles**", apiPrefix))
                                                        .permitAll()
                                                        .requestMatchers(GET,
                                                                        String.format("%s/categories/**", apiPrefix))
                                                        .permitAll()
                                                        .requestMatchers(GET,
                                                                        String.format("%s/products/**", apiPrefix))
                                                        .permitAll()
                                                        .requestMatchers(GET,
                                                                        String.format("%s/products/images/*",
                                                                                        apiPrefix))
                                                        .permitAll()
                                                        .requestMatchers(GET,
                                                                        String.format("%s/users/profile-images/**",
                                                                                        apiPrefix))
                                                        .permitAll()

                                                        // Cart endpoints - require authentication or allow guest access
                                                        .requestMatchers(String.format("%s/cart/**", apiPrefix))
                                                        .permitAll()

                                                        // Orders - require authentication (handled by @PreAuthorize)
                                                        .requestMatchers(String.format("%s/orders/**", apiPrefix))
                                                        .authenticated()
                                                        .requestMatchers(
                                                                        String.format("%s/order_details/**", apiPrefix))
                                                        .authenticated()

                                                        // Payment endpoints - require authentication
                                                        .requestMatchers(String.format("%s/payments/**", apiPrefix))
                                                        .authenticated()

                                                        // Admin endpoints - will be handled by @PreAuthorize
                                                        .requestMatchers(String.format("%s/admin/**", apiPrefix))
                                                        .authenticated()

                                                        .anyRequest()
                                                        .authenticated();
                                })
                                .csrf(AbstractHttpConfigurer::disable)
                                .oauth2Login(Customizer.withDefaults())
                                .oauth2ResourceServer(c -> c.opaqueToken(Customizer.withDefaults()));
                http.securityMatcher(String.valueOf(EndpointRequest.toAnyEndpoint()));
                return http.build();
        }
}
