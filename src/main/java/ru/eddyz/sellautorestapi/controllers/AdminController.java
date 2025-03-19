package ru.eddyz.sellautorestapi.controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.eddyz.sellautorestapi.dto.BrandBaseDto;
import ru.eddyz.sellautorestapi.dto.ColorBaseDto;
import ru.eddyz.sellautorestapi.dto.ModelBaseDto;
import ru.eddyz.sellautorestapi.entities.Brand;
import ru.eddyz.sellautorestapi.entities.Color;
import ru.eddyz.sellautorestapi.entities.Model;
import ru.eddyz.sellautorestapi.exeptions.AdNotFountException;
import ru.eddyz.sellautorestapi.service.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AccountService accountService;
    private final RefreshTokenService refreshTokenService;
    private final ColorService colorService;
    private final BrandService brandService;
    private final ModelService modelService;
    private final CsvDataService csvDataService;


    @PostMapping("/ban/{accountId}")
    public ResponseEntity<?> ban(@PathVariable("accountId") Long AccountId) {
        var account = accountService.findById(AccountId)
                .orElseThrow(() -> new AdNotFountException("Account not found"));

        account.setBlocked(true);
        accountService.update(account);
        account.getRefreshToken().forEach(refreshToken -> {
            refreshToken.setBlocked(true);
            refreshTokenService.update(refreshToken);
        });

        return ResponseEntity.ok()
                .build();
    }

    @PostMapping("/colors")
    public ResponseEntity<?> createColor(@RequestBody ColorBaseDto colorDto) {
        var color = Color.builder()
                .title(colorDto.getTitle())
                .build();

        return ResponseEntity.ok(colorService.save(color));
    }

    @DeleteMapping("/colors/{colorId}")
    public ResponseEntity<?> deleteColor(@PathVariable("colorId") Integer colorId) {
        colorService.deleteById(colorId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/brands")
    public ResponseEntity<?> createBrand(@RequestBody BrandBaseDto brandBaseDto) {
        var brand = Brand.builder()
                .title(brandBaseDto.getTitle())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(brandService.save(brand));
    }

    @DeleteMapping("/brands/{brandId}")
    public ResponseEntity<?> deleteBrand(@PathVariable Integer brandId) {
        brandService.deleteById(brandId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/models/{brandTitle}")
    public ResponseEntity<?> createModels(@RequestBody ModelBaseDto modelBaseDto, @PathVariable String brandTitle) {
        var brand = brandService.findByTitle(brandTitle);
        var model = Model.builder()
                .brand(brand)
                .title(modelBaseDto.getTitle())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modelService.save(model));
    }

    @DeleteMapping("/models/{modelId}")
    public ResponseEntity<?> deleteModel(@PathVariable Integer modelId) {
        modelService.deleteByid(modelId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/backup/upload", consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE,
    })
    public ResponseEntity<?> uploadBackup(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "File is empty"));
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.endsWith(".zip")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "File extension is empty"));
        }

        try {
            csvDataService.importFromCsv(file.getInputStream());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "CSV file could not be imported"));
        }

        return ResponseEntity.ok().build();
    }


    @GetMapping("/backup/download")
    public ResponseEntity<?> downloadBackup() {
        var backup = csvDataService.exportToCsv();
        if (backup.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "CSV file could not be download"));
        }
        try {
            var file = backup.get();
            Path filePath = Paths.get(file.getPath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
