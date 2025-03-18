package ru.eddyz.sellautorestapi.service;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.eddyz.sellautorestapi.entities.Model;
import ru.eddyz.sellautorestapi.repositories.ModelRepository;

@Service
@RequiredArgsConstructor
public class ModelService {

    private final ModelRepository modelRepository;

    public Model findByTitleAndBrandTitle(String modelTitle, String brandTitle) {
        return modelRepository.findByTitleAndBrandTitle(modelTitle, brandTitle)
                .orElseThrow(() -> new EntityNotFoundException("Model not found"));
    }
}
