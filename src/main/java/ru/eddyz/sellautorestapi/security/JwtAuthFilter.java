package ru.eddyz.sellautorestapi.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.Nullable;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.eddyz.sellautorestapi.service.RefreshTokenService;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {


    private final JwtService jwtService;

    private final static String BEARER_PREFIX = "Bearer ";
    private final UserDetailsService accountService;
    private final RefreshTokenService refreshTokenService;


    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @Nullable HttpServletResponse response,
                                    @NotNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String email;
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(BEARER_PREFIX.length());

        var jwtOpt = refreshTokenService.findByToken(jwt);

        if (jwtOpt.isPresent()) {
            var token = jwtOpt.get();
            if (token.isBlocked()) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        try {
            email = jwtService.extractEmail(jwt);
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = accountService.loadUserByUsername(email);
                if (jwtService.validateToken(jwt, userDetails)) {
                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    if (userDetails.isAccountNonLocked()) {
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            if (response != null) {
                response.sendError(401, "Token is expired");
            }
        }
    }
}
