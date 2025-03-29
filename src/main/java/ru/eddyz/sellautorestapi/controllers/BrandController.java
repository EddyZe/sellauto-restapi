package ru.eddyz.sellautorestapi.controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.eddyz.sellautorestapi.dto.BrandsDto;
import ru.eddyz.sellautorestapi.mapper.BrandBaseMapper;
import ru.eddyz.sellautorestapi.service.BrandService;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;
    private final BrandBaseMapper brandBaseMapper;

    @GetMapping
    public ResponseEntity<?> getAllBrands() {
        return ResponseEntity.ok(BrandsDto.builder()
                .brands(brandService.findAll()
                        .stream()
                        .map(brandBaseMapper::toDetailsDto)
                        .toList())
                .build());
    }

    @GetMapping("/{brandId}")
    public ResponseEntity<?> getBrandById(@PathVariable("brandId") Integer brandId) {
        return ResponseEntity.ok(brandBaseMapper.toDetailsDto(brandService.findById(brandId)));
    }
}
