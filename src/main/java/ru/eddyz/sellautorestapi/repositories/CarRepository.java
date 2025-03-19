package ru.eddyz.sellautorestapi.repositories;


import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.eddyz.sellautorestapi.entities.Car;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    Optional<Car> findByVin(String vin);

    List<Car> findByBrandTitle(String title);

    @Query("select c from Car c join Color cl on c.color.colorId=cl.colorId where cl.title = :colorTitle")
    List<Car> findByColorTitle(@Param("colorTitle")String colorTitle);

    List<Car> findByBrandTitleAndModelTitle(String title, String modelTitle);

    List<Car> findByBrandTitleAndModelTitleAndColorTitle(String brandTitle, String modelTitle, String colorTitle, Sort sort);
}
