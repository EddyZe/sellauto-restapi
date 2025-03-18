package ru.eddyz.sellautorestapi.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.eddyz.sellautorestapi.entities.Ad;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdRepository extends JpaRepository<Ad, Long> {

    Optional<Ad> findByCarVin(String vin);

    @Query(value = "select a from Ad a join User u on a.user.userId=u.userId where u.userId=:userId")
    List<Ad> findAdUser(@Param("userId") Long userId);

    @Query(value = "select a from Ad a join Account acc on acc.user.userId=a.user.userId where acc.email=:email")
    List<Ad> findAdUserByEmail(@Param("email") String email);
}
