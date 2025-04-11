package ru.eddyz.sellautorestapi.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ru.eddyz.sellautorestapi.entities.RefreshToken;
import ru.eddyz.sellautorestapi.security.JwtAuthFilter;
import ru.eddyz.sellautorestapi.security.JwtService;
import ru.eddyz.sellautorestapi.service.AccountService;
import ru.eddyz.sellautorestapi.service.RefreshTokenService;

import java.util.Date;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final AccountService accountService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/topic/**",
                                "/app/**",
                                "/ws/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**"
                        ).permitAll()

                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/app.js",
                                "/styles/**",
                                "/static/**",
                                "/photos/**",
                                "/error"
                        ).permitAll()


                        .requestMatchers(HttpMethod.OPTIONS).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/ads/**",
                                "/api/v1/auth/**",
                                "/api/v1/models/**",
                                "/api/v1/brands/**",
                                "/api/v1/colors/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/auth/**",
                                "/api/v1/ads/filter"
                        ).permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").authenticated()

                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN") // Используем hasRole вместо hasAuthority

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .logout(logout -> logout
                        .addLogoutHandler((request, response, authentication) -> {
                                    if (authentication != null) {
                                        accountService.logout(authentication.getName());
                                    }
                                    var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
                                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                                        var token = authHeader.substring(7);
                                        var email = jwtService.extractEmail(token);

                                        accountService.findByEmail(email)
                                                .ifPresent(account -> refreshTokenService.save(RefreshToken.builder()
                                                        .token(token)
                                                        .blocked(true)
                                                        .account(account)
                                                        .expiredDate(new Date())
                                                        .build()));

                                        accountService.logout(email);
                                    }
                                    if (request.getSession(false) != null) {
                                        request.getSession(false).invalidate();
                                    }
                                    SecurityContextHolder.clearContext();
                                    log.info("logout successful");
                                }
                        )
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Stomp-Action", "Stomp-Destination"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}