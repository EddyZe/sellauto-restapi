package ru.eddyz.sellautorestapi.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import ru.eddyz.sellautorestapi.entities.Car;
import ru.eddyz.sellautorestapi.entities.Chat;
import ru.eddyz.sellautorestapi.exeptions.CarNotFoundException;
import ru.eddyz.sellautorestapi.repositories.CarRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;

    public List<Car> findByVin(String vin){
        return carRepository.findByVin(vin);
    }

    public List<Car> findByBrandTitle(String title) {
        return carRepository.findByBrandTitle(title);
    }


    public List<Car> findByColorTitle(String colorTitle) {
        return carRepository.findByColorTitle(colorTitle);
    }

    public List<Car> findByBrandTitleAndModelTitle(String title, String modelTitle) {
        return carRepository.findByBrandTitleAndModelTitle(title, modelTitle);
    }

    public List<Car> findByBrandTitleAndModelTitleAndColorTitle(String brandTitle, String modelTitle, String colorTitle) {
        return carRepository.findByBrandTitleAndModelTitleAndColorTitle(brandTitle, modelTitle, colorTitle);
    }

    public List<Car> findAll() {
        return carRepository.findAll();
    }
}
