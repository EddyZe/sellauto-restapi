package ru.eddyz.sellautorestapi.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.eddyz.sellautorestapi.entities.Model;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModelRepository extends JpaRepository<Model, Integer> {

    Optional<Model> findByTitleAndBrandTitle(String modelTitle, String brandTitle);
    List<Model> findByBrandBrandId(Integer brandId);

}
