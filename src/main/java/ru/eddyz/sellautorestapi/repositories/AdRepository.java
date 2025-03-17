package ru.eddyz.sellautorestapi.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.eddyz.sellautorestapi.entities.Ad;

import java.util.Optional;

@Repository
public interface AdRepository extends JpaRepository<Ad, Long> {

    Optional<Ad> findByCarVin(String vin);
}
