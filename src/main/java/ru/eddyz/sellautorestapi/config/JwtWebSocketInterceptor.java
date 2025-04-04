package ru.eddyz.sellautorestapi.config;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import ru.eddyz.sellautorestapi.security.JwtService;

import java.util.HashMap;


@Component
@RequiredArgsConstructor
public class JwtWebSocketInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtWebSocketInterceptor.class);
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;


    @Override
    public Message<?> preSend(@Nullable Message<?> message, @Nullable MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        log.info("All headers: {}", accessor.toNativeHeaderMap());
        StompCommand command = accessor.getCommand();

        if (command == StompCommand.CONNECT ||
            command == StompCommand.SEND ||
            command == StompCommand.SUBSCRIBE) {

            String authHeader = accessor.getFirstNativeHeader("Authorization");
            log.info("Auth header: {}", authHeader);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    String email = jwtService.extractEmail(token);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    if (jwtService.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        accessor.setUser(authentication);

                        if (accessor.getSessionAttributes() == null) {
                            accessor.setSessionAttributes(new HashMap<>());
                        }
                        accessor.getSessionAttributes().put(
                                "SPRING_SECURITY_CONTEXT",
                                SecurityContextHolder.getContext()
                        );

                        log.info("User authenticated: {}", userDetails.getUsername());
                    }
                } catch (Exception e) {
                    log.error("Authentication error: ", e);
                    throw new AuthenticationCredentialsNotFoundException("Invalid token", e);
                }
            } else {
                log.warn("Authorization header is missing or invalid");
            }
        }
        return message;
    }
}
