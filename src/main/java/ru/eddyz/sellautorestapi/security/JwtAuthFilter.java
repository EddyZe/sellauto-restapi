package ru.eddyz.sellautorestapi.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.Nullable;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
                                    @Nullable FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String jwt;

        if (filterChain == null)
            return;

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            if (response != null && isNotPublishAdr(request)) {
                response.sendError(HttpStatus.UNAUTHORIZED.value());
                return;
            }

            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(BEARER_PREFIX.length());

        var jwtOpt = refreshTokenService.findByToken(jwt);

        if (jwtOpt.isPresent()) {
            var token = jwtOpt.get();
            if (token.isBlocked()) {
                if (response != null && isNotPublishAdr(request)) {
                    response.sendError(HttpStatus.UNAUTHORIZED.value());
                    return;
                }
            }
        }

        validateToken(request, response, filterChain, jwt);
    }

    private void validateToken(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain, String jwt) throws IOException, ServletException {
        final String email;
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
                } else {
                    if (response != null && isNotPublishAdr(request)) {
                        response.sendError(HttpStatus.UNAUTHORIZED.value());
                        return;
                    }
                }
            } else {
                if (response != null && isNotPublishAdr(request)) {
                    response.sendError(HttpStatus.UNAUTHORIZED.value());
                    return;
                }
            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            if (response != null && isNotPublishAdr(request)) {
                response.sendError(401, "Token is expired");
            }
        }
    }

    private boolean isNotPublishAdr(HttpServletRequest request) {
        var path = request.getServletPath();
        var method = request.getMethod();

        if (path.startsWith("/api/v1/auth") || path.startsWith("/topic") || path.startsWith("/app") ||
            path.startsWith("/ws") || method.equalsIgnoreCase(HttpMethod.OPTIONS.toString()) ||
            path.startsWith("/error") || path.startsWith("/photos") || path.startsWith("/static") ||
            path.startsWith("/styles") || path.startsWith("/index") || path.startsWith("/swagger-ui") ||
            path.startsWith("/v3/api-docs"))
            return false;

        if (method.equalsIgnoreCase(HttpMethod.GET.toString())) {
            if (path.startsWith("/api/v1/ads") ||
                path.startsWith("/api/v1/models") ||
                path.startsWith("/api/v1/brands") ||
                path.startsWith("/api/v1/colors"))
                return false;
        }

        if(method.equalsIgnoreCase(HttpMethod.POST.toString())) {
            return !path.startsWith("/api/v1/ads/filter");
        }


        return true;
    }
}
