package ru.eddyz.sellautorestapi.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.eddyz.sellautorestapi.entities.Car;
import ru.eddyz.sellautorestapi.exeptions.CarException;
import ru.eddyz.sellautorestapi.repositories.CarRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;

    public Optional<Car> findByVin(String vin) {
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
        return carRepository.findByBrandTitleAndModelTitleAndColorTitle(brandTitle, modelTitle, colorTitle, Sort.by("createdAt"));
    }

    public List<Car> findAll() {
        return carRepository.findAll();
    }

    public Car save(Car car) {
        if (carRepository.findByVin(car.getVin()).isPresent())
            throw new CarException("Car already exists");
        return carRepository.save(car);
    }
}
