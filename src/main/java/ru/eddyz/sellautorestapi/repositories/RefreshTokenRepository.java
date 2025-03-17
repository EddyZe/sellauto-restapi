package ru.eddyz.sellautorestapi.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.eddyz.sellautorestapi.entities.RefreshToken;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String refreshToken);

    List<RefreshToken> findByExpiredDateBefore(Date  date);
}
