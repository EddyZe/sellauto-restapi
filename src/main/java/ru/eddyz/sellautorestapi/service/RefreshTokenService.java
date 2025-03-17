package ru.eddyz.sellautorestapi.service;


import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.eddyz.sellautorestapi.entities.RefreshToken;
import ru.eddyz.sellautorestapi.exeptions.AuthException;
import ru.eddyz.sellautorestapi.repositories.RefreshTokenRepository;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;


    public void save(RefreshToken refreshToken) {
        var token = refreshTokenRepository.findByToken(refreshToken.getToken());
        if (token.isPresent()) {
            throw new AuthException("Token already exists", "TOKEN_ALREADY_EXISTS");
        }

        refreshTokenRepository.save(refreshToken);
    }

    public void update(RefreshToken refreshToken) {
        refreshTokenRepository.findByToken(refreshToken.getToken())
                .ifPresent(rt -> refreshTokenRepository.save(refreshToken));
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public void blockedRefreshToken(String refreshToken) {
        var token = refreshTokenRepository.findByToken(refreshToken);

        if (token.isEmpty()) {
            throw new AuthException("Token not found", "TOKEN_NOT_FOUND");
        }

        if (token.get().isBlocked()) {
            throw new AuthException("Token blocked", "TOKEN_ALREADY_BLOCKED");
        }

        token.get().setBlocked(true);
        refreshTokenRepository.save(token.get());

    }


    @Scheduled(fixedDelay = 20, timeUnit = TimeUnit.MINUTES)
    public void removeRefreshTokenAfter() {
        refreshTokenRepository.findByExpiredDateAfter(new Date())
                .forEach(token -> refreshTokenRepository.deleteById(token.getId()));
    }

}
