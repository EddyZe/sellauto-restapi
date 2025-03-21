package ru.eddyz.sellautorestapi.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.eddyz.sellautorestapi.security.JwtAuthFilter;
import ru.eddyz.sellautorestapi.service.AccountService;
import ru.eddyz.sellautorestapi.service.RefreshTokenService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {


    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final AccountService accountService;
    private final RefreshTokenService refreshTokenService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authManager -> {
                    authManager.requestMatchers(HttpMethod.OPTIONS).permitAll();
                    authManager.requestMatchers(HttpMethod.GET, "api/v1/ads/**", "/api/v1/auth/**",
                            "/error", "api/v1/models/**",
                            "/styles/**",
                            "/photos/**").permitAll();
                    authManager.requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").authenticated();
                    authManager.requestMatchers(HttpMethod.POST, "/api/v1/auth/**", "/api/v1/ads/filter", "/errors").permitAll();
                    authManager.requestMatchers(HttpMethod.GET, "/api/v1/admin/**").hasAuthority("ROLE_ADMIN");
                    authManager.requestMatchers(HttpMethod.POST, "/api/v1/admin/**").hasAuthority("ROLE_ADMIN");

                    authManager.anyRequest()
                            .authenticated();
                })
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .logout(logout -> logout.addLogoutHandler((request, response, authentication) ->
                        accountService.findByEmail(authentication.getName())
                        .ifPresent(acc -> acc.getRefreshToken()
                                .forEach(refreshToken -> {
                                    refreshToken.setBlocked(true);
                                    refreshTokenService.save(refreshToken);
                                }))))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}

