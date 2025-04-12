package ru.eddyz.sellautorestapi.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.eddyz.sellautorestapi.dto.ColorBaseDto;
import ru.eddyz.sellautorestapi.dto.ColorsDto;
import ru.eddyz.sellautorestapi.mapper.ColorBaseMapper;
import ru.eddyz.sellautorestapi.service.ColorService;

@RestController
@RequestMapping("/api/v1/colors")
@RequiredArgsConstructor
@Tag(name = "Цвета авто")
public class ColorsController {

    private final ColorService colorService;
    private final ColorBaseMapper colorBaseMapper;


    @GetMapping
    @Operation(
            summary = "Все цвета",
            description = "Позволяет получить список созданных цветов",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = ColorsDto.class))
                    )
            }
    )
    public ResponseEntity<?> getColors() {
        return ResponseEntity.ok(
                ColorsDto.builder()
                        .colors(colorService.findAll()
                                .stream()
                                .map(colorBaseMapper::toDto)
                                .toList())
                        .build()
        );
    }

    @GetMapping("/{title}")
    @Operation(
            summary = "Цвет",
            description = "Позволяет получить цвет по его названию",
            parameters = {
                    @Parameter(name = "title", description = "Название цвета")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = ColorBaseDto.class))
                    )
            }
    )
    public ResponseEntity<?> getColor(@PathVariable String title) {
        return ResponseEntity.ok(
                colorBaseMapper.toDto(colorService.findByTitle(title))
        );
    }
}
