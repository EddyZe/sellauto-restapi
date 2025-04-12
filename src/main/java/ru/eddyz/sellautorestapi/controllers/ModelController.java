package ru.eddyz.sellautorestapi.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.eddyz.sellautorestapi.dto.ModelBaseDto;
import ru.eddyz.sellautorestapi.dto.ModelsBaseDto;
import ru.eddyz.sellautorestapi.mapper.ModelsBaseMapper;
import ru.eddyz.sellautorestapi.service.ModelService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/models")
@Tag(name = "Модели авто")
public class ModelController {


    private final ModelService modelService;
    private final ModelsBaseMapper modelsBaseMapper;


    @GetMapping
    @Operation(
            summary = "Список моделей",
            description = "Позволяет получить список созданных моделей",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = ModelsBaseDto.class))
                    )
            }
    )
    public ResponseEntity<?> getModels() {
        return ResponseEntity
                .ok(ModelsBaseDto.builder()
                        .models(modelService.findAll()
                                .stream()
                                .map(modelsBaseMapper::toDto)
                                .toList())
                        .build());
    }

    @GetMapping("/{modelId}")
    @Operation(
            summary = "Получить модель по ID",
            description = "Позволяет получить модель по ID",
            parameters = {
                    @Parameter(name = "modelId", description = "ID модели")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = ModelBaseDto.class))
                    ),
                    @ApiResponse(
                            description = "Ошибка",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
                    )
            }
    )
    public ResponseEntity<?> getModelById(@PathVariable("modelId") Integer modelId) {
        return ResponseEntity
                .ok(modelsBaseMapper.toDto(modelService.findById(modelId)));
    }
}
