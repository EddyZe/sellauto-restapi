package ru.eddyz.sellautorestapi.controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.eddyz.sellautorestapi.mapper.ModelsBaseMapper;
import ru.eddyz.sellautorestapi.service.ModelService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/models")
public class ModelController {


    private final ModelService modelService;
    private final ModelsBaseMapper modelsBaseMapper;


    @GetMapping
    public ResponseEntity<?> getModels() {
        return ResponseEntity
                .ok(modelService.findAll()
                        .stream()
                        .map(modelsBaseMapper::toDto)
                        .toList());
    }

    @GetMapping("/{modelsId}")
    public ResponseEntity<?> getModelById(@PathVariable("modelsId") Integer modelId) {
        return ResponseEntity
                .ok(modelsBaseMapper.toDto(modelService.findById(modelId)));
    }
}
