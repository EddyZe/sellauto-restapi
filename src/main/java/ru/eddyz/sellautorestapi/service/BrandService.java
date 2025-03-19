package ru.eddyz.sellautorestapi.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.eddyz.sellautorestapi.entities.Brand;
import ru.eddyz.sellautorestapi.exeptions.BrandException;
import ru.eddyz.sellautorestapi.exeptions.BrandNotFoundException;
import ru.eddyz.sellautorestapi.repositories.BrandRepository;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    public Brand findByTitle(String title) {
        return brandRepository.findByTitle(title)
                .orElseThrow(() -> new BrandNotFoundException("Brand not found"));
    }

    public Brand save(Brand brand) {
        if (brandRepository.findByTitle(brand.getTitle()).isPresent()) {
            throw new BrandException("Brand already exists");
        }

        return brandRepository.save(brand);
    }

    public void deleteById(Integer brandId) {
        brandRepository.findById(brandId)
                .ifPresent(brand -> brandRepository.deleteById(brandId));
    }
}
