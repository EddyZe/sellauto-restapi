package ru.eddyz.sellautorestapi.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.eddyz.sellautorestapi.entities.ForgotPasswordCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ForgotPasswordCodeRepository extends JpaRepository<ForgotPasswordCode, Long> {


    @Query("select c from ForgotPasswordCode as c join User u on u.userId=c.user.userId where u.account.email=:userEmail and c.code=:code")
    Optional<ForgotPasswordCode> findByCodeAndUserEmail(@Param("code") String code, @Param("userEmail") String userEmail);


    @Query("select c from ForgotPasswordCode c where c.expiredAt>=:currentDataTime")
    List<ForgotPasswordCode> findExpiredCodes(@Param("currentDataTime") LocalDateTime currentDateTime);

    List<ForgotPasswordCode> findByActive(Boolean active);
}
