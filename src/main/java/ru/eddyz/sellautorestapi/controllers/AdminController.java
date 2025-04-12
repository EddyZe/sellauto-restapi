package ru.eddyz.sellautorestapi.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.eddyz.sellautorestapi.dto.*;
import ru.eddyz.sellautorestapi.entities.Brand;
import ru.eddyz.sellautorestapi.entities.Color;
import ru.eddyz.sellautorestapi.entities.Model;
import ru.eddyz.sellautorestapi.exeptions.AccountException;
import ru.eddyz.sellautorestapi.exeptions.AccountNotFoundException;
import ru.eddyz.sellautorestapi.exeptions.AdNotFountException;
import ru.eddyz.sellautorestapi.mapper.BrandBaseMapper;
import ru.eddyz.sellautorestapi.mapper.ColorBaseMapper;
import ru.eddyz.sellautorestapi.mapper.ModelsBaseMapper;
import ru.eddyz.sellautorestapi.mapper.UserMapper;
import ru.eddyz.sellautorestapi.service.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Контроллер для администратора", description = "Функции администратора")
public class AdminController {

    private final AccountService accountService;
    private final RefreshTokenService refreshTokenService;
    private final ColorService colorService;
    private final BrandService brandService;
    private final ModelService modelService;
    private final CsvDataService csvDataService;
    private final ColorBaseMapper colorBaseMapper;
    private final BrandBaseMapper brandBaseMapper;
    private final ModelsBaseMapper modelsBaseMapper;
    private final UserMapper userMapper;
    private final UserService userService;


    @PostMapping("/ban/{accountId}")
    @Operation(
            summary = "Блокировка пользователя",
            description = "Блокирует пользователя по accountId",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешный запрос"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
                    ),
                    @ApiResponse(
                            responseCode = "401"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Недостаточно прав"
                    )
            }
    )
    public ResponseEntity<?> ban(@PathVariable("accountId") Long AccountId, @AuthenticationPrincipal UserDetails userDetails) {
        var account = accountService.findById(AccountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        if (account.getEmail().equals(userDetails.getUsername())) {
            throw new AccountException("You can't block yourself.", "INVALID_USER");
        }

        account.setBlocked(true);
        accountService.update(account);
        account.getRefreshToken().forEach(refreshToken -> {
            refreshToken.setBlocked(true);
            refreshTokenService.update(refreshToken);
        });

        return ResponseEntity.ok()
                .build();
    }

    @PostMapping("/unban/{accountId}")
    @Operation(
            summary = "Снятие блокировки",
            description = "Снимает блокировку с пользователя по ID аккаунте",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешный запрос"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
                    ),
                    @ApiResponse(responseCode = "401"),
                    @ApiResponse(responseCode = "403")
            }
    )
    public ResponseEntity<?> unBan(@PathVariable("accountId") Long AccountId) {
        var account = accountService.findById(AccountId)
                .orElseThrow(() -> new AdNotFountException("Account not found"));

        account.setBlocked(false);
        accountService.update(account);
        account.getRefreshToken().forEach(refreshToken -> {
            refreshToken.setBlocked(false);
            refreshTokenService.update(refreshToken);
        });

        return ResponseEntity.ok()
                .build();
    }

    @GetMapping("/users")
    @Operation(
            summary = "Список пользователей",
            description = "Возвращает список пользователей",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешный запрос",
                            content = @Content(schema = @Schema(implementation = ProfilesDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "401"
                    ),
                    @ApiResponse(responseCode = "403")
            }
    )
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.ok(
                ProfilesDto.builder()
                        .profiles(userService.findAll()
                                .stream()
                                .map(userMapper::toDto)
                                .toList())
                        .build()
        );
    }

    @PostMapping("/colors")
    @Operation(
            summary = "Создание цвета для авто",
            description = "Создает цвет для авто",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные цвета",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ColorBaseDto.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Успешный запрос",
                            content = @Content(schema = @Schema(implementation = Color.class))
                    ),
                    @ApiResponse(
                            responseCode = "401"
                    ),
                    @ApiResponse(responseCode = "403"),
                    @ApiResponse(responseCode = "404",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            }
    )
    public ResponseEntity<?> createColor(@RequestBody ColorBaseDto colorDto) {
        var color = Color.builder()
                .title(colorDto.getTitle())
                .build();

        return ResponseEntity.ok(colorService.save(color));
    }

    @GetMapping("/colors")
    @Operation(
            summary = "Список цветов",
            description = "Отправляет список созданных цветов",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = ColorBaseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
                    ),
                    @ApiResponse(responseCode = "401"),
                    @ApiResponse(responseCode = "403")
            }
    )
    public ResponseEntity<?> getColors() {
        return ResponseEntity.ok(ColorsDto
                .builder()
                .colors(colorService.findAll()
                        .stream()
                        .map(colorBaseMapper::toDto)
                        .toList())
                .build());
    }

    @DeleteMapping("/colors/{colorId}")
    @Operation(
            summary = "Удаление цвета",
            description = "Удаляет цвет по ID",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200"
                    ),
                    @ApiResponse(responseCode = "404", description = "Цвет не найден"),
                    @ApiResponse(responseCode = "401"),
                    @ApiResponse(responseCode = "403")
            }
    )
    public ResponseEntity<?> deleteColor(@PathVariable("colorId") Integer colorId) {
        colorService.deleteById(colorId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/brands")
    @Operation(
            summary = "Создание бренда",
            description = "Cоздает новый бренд",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = BrandBaseDto.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            content = @Content(schema = @Schema(implementation = Brand.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
                    ),
                    @ApiResponse(responseCode = "401"),
                    @ApiResponse(responseCode = "403")
            }
    )
    public ResponseEntity<?> createBrand(@RequestBody BrandBaseDto brandBaseDto) {
        var brand = Brand.builder()
                .title(brandBaseDto.getTitle())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(brandService.save(brand));
    }

    @GetMapping("/brands")
    @Operation(
            summary = "Список брендов",
            description = "Возвращает список созданных брендов",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = BrandsDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "401"
                    ),
                    @ApiResponse(responseCode = "403")
            }
    )
    public ResponseEntity<?> getBrands() {
        return ResponseEntity.ok(
                BrandsDto.builder()
                        .brands(brandService.findAll()
                                .stream()
                                .map(brandBaseMapper::toDetailsDto)
                                .toList())
                        .build());
    }

    @GetMapping("/brands/{brandId}")
    @Operation(
            summary = "Получить бренд по ID",
            description = "Возвращает бренд по его ID",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = BrandDetailsDto.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Бренд не найден"),
                    @ApiResponse(responseCode = "401"),
                    @ApiResponse(responseCode = "403")
            }
    )
    public ResponseEntity<?> getBrand(@PathVariable("brandId") Integer brandId) {
        return ResponseEntity.ok(
                brandBaseMapper.toDetailsDto(
                        brandService.findById(brandId)
                )
        );
    }

    @DeleteMapping("/brands/{brandId}")
    @Operation(
            summary = "Удаление бренда",
            description = "Удаляет бренд по его ID",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Бренд с таким ID не найден",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
                    ),
                    @ApiResponse(responseCode = "403"),
                    @ApiResponse(responseCode = "401")
            }
    )
    public ResponseEntity<?> deleteBrand(@PathVariable Integer brandId) {
        brandService.deleteById(brandId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/models/{brandTitle}")
    @Operation(
            summary = "Добавление модели к бренду",
            description = "Добавляет модель по названию бренда",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "данные модели",
                    content = @Content(schema = @Schema(implementation = ModelBaseDto.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            content = @Content(schema = @Schema(implementation = Model.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
                    ),
                    @ApiResponse(
                            responseCode = "401"
                    ),
                    @ApiResponse(responseCode = "403")
            }

    )
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

    @GetMapping("/models/{modelId}")
    @Operation(
            summary = "Получение модели",
            description = "Получение модели по его ID",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = ModelsBaseMapper.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
                    ),
                    @ApiResponse(responseCode = "401"),
                    @ApiResponse(responseCode = "403")
            }
    )
    public ResponseEntity<?> getModel(@PathVariable Integer modelId) {
        return ResponseEntity.ok(
                modelsBaseMapper.toDto(modelService.findById(modelId))
        );
    }

    @GetMapping("/brand/{brandTitle}/models")
    @Operation(
            summary = "Модели бренда",
            description = "Получить список моделей бренда по его названию",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = ModelsDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "401"
                    ),
                    @ApiResponse(
                            responseCode = "403"
                    )
            }
    )
    public ResponseEntity<?> getModels(@PathVariable String brandTitle) {
        return ResponseEntity.ok(
                ModelsDto.builder()
                        .models(brandService.findByTitle(brandTitle)
                                .getModel()
                                .stream()
                                .map(modelsBaseMapper::toDto)
                                .toList())
                        .build()
        );
    }

    @PostMapping(value = "/backup/upload", consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE,
    })
    @Operation(
            summary = "Загрузка бэкапа",
            description = "Устанавливает бэкап. Принимает ZIP файл с бекапом",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
                    ),
                    @ApiResponse(responseCode = "401"),
                    @ApiResponse(responseCode = "403")
            }
    )
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


    @CrossOrigin("*")
    @GetMapping("/backup/download")
    @Operation(
            summary = "Скачать бекап",
            description = "Позволяет создать и скачать бэкап CSV",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401"),
                    @ApiResponse(responseCode = "403"),
                    @ApiResponse(responseCode = "500")
            }
    )
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
