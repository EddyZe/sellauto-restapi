package ru.eddyz.sellautorestapi.controllers;


import io.swagger.v3.oas.annotations.Operation;
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
import ru.eddyz.sellautorestapi.dto.BrandDetailsDto;
import ru.eddyz.sellautorestapi.dto.BrandsDto;
import ru.eddyz.sellautorestapi.mapper.BrandBaseMapper;
import ru.eddyz.sellautorestapi.service.BrandService;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
@Tag(name = "Бренды авто")
public class BrandController {

    private final BrandService brandService;
    private final BrandBaseMapper brandBaseMapper;

    @GetMapping
    @Operation(
            summary = "Получить все бренды",
            description = "Отображает список вех созданных брендов",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = BrandsDto.class))
                    )
            }
    )
    public ResponseEntity<?> getAllBrands() {
        return ResponseEntity.ok(BrandsDto.builder()
                .brands(brandService.findAll()
                        .stream()
                        .map(brandBaseMapper::toDetailsDto)
                        .toList())
                .build());
    }

    @GetMapping("/{brandId}")
    @Operation(
            summary = "Получить бренд по ID",
            description = "Позволяет получить бренд по его ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = BrandDetailsDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
                    )
            }
    )
    public ResponseEntity<?> getBrandById(@PathVariable("brandId") Integer brandId) {
        return ResponseEntity.ok(brandBaseMapper.toDetailsDto(brandService.findById(brandId)));
    }
}
