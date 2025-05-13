package ru.eddyz.sellautorestapi.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.eddyz.sellautorestapi.dto.*;
import ru.eddyz.sellautorestapi.entities.Ad;
import ru.eddyz.sellautorestapi.entities.Car;
import ru.eddyz.sellautorestapi.entities.Price;
import ru.eddyz.sellautorestapi.entities.User;
import ru.eddyz.sellautorestapi.enums.Role;
import ru.eddyz.sellautorestapi.exeptions.AccountNotFoundException;
import ru.eddyz.sellautorestapi.exeptions.AdException;
import ru.eddyz.sellautorestapi.mapper.AdDetailsMapper;
import ru.eddyz.sellautorestapi.service.*;
import ru.eddyz.sellautorestapi.util.BindingResultHelper;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ads")
@Tag(name = "Объявления", description = "Управление объявлениями")
public class AdController {

    private final AdService adService;
    private final CarService carService;
    private final PhotoService photoService;
    private final UserService userService;
    private final AccountService accountService;


    private final AdDetailsMapper adDetailsMapper;
    private final PriceService priceService;

    @GetMapping("/my")
    @Operation(
            summary = "Получить список объявлений текущего пользователя",
            description = "Возвращает список объявлений текущего пользователя",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешный запрос",
                            content = @Content(schema = @Schema(implementation = AdUserAds.class))
                    ),
                    @ApiResponse(
                            responseCode = "401"
                    )
            }
    )
    public ResponseEntity<?> getMyAds(@AuthenticationPrincipal UserDetails userDetails) {
        var ads = adService.findByAdsByUserEmail(userDetails.getUsername())
                .stream()
                .map(adDetailsMapper::toDto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ads);
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Получить список объявлений пользователя по ID",
            description = "Возвращает список объявлений пользователя",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешный запрос",
                            content = @Content(schema = @Schema(implementation = AdUserAds.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Пользователь не найден"
                    )
            }
    )
    public ResponseEntity<?> getAdUser(@PathVariable("userId") Long userId) {
        var user = userService.findById(userId);
        return ResponseEntity.ok(
                AdUserAds.builder()
                        .ads(adService.findByAdsByUserEmail(user.getAccount().getEmail())
                                .stream()
                                .map(adDetailsMapper::toDto)
                                .toList())
                        .build()
        );
    }

    @GetMapping
    @Operation(
            summary = "Получить список объявлений",
            description = "Возвращает список объявлений",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешный запрос",
                            content = @Content(schema = @Schema(implementation = AdUserAds.class))
                    )
            }
    )
    public ResponseEntity<?> getAds(@RequestParam(name = "color", required = false) String color,
                                    @RequestParam(name = "brand", required = false) String brand,
                                    @RequestParam(name = "model", required = false) String model,
                                    @RequestParam(name = "year-from", required = false) String yearFrom,
                                    @RequestParam(name = "year-to", required = false) String yearTo,
                                    @RequestParam(name = "sort-year", required = false) String sortYear,
                                    @RequestParam(name = "sort-price", required = false) String sortPrice,
                                    @RequestParam(name = "price-from", required = false) String priceFrom,
                                    @RequestParam(name = "price-to", required = false) String priceTo,
                                    @RequestParam(name = "mileage-from", required = false) String mileageFrom,
                                    @RequestParam(name = "mileage-to", required = false) String mileageTo,
                                    @RequestParam(name = "transmission", required = false) String transmission,
                                    @RequestParam(name = "engine", required = false) String engine,
                                    @RequestParam(name = "body", required = false) String body,
                                    @RequestParam(name = "drive", required = false) String drive) {
        var ads = new ArrayList<>(carService.findAll()
                .stream()
                .filter(car -> matcherFilter(color, brand, model, yearFrom, yearTo, priceFrom, priceTo, mileageFrom, mileageTo, transmission, engine, body, drive, car))
                .map(car -> adDetailsMapper.toDto(car.getAd()))
                .toList());

        if (sortYear != null) {
            if (sortYear.equalsIgnoreCase("acs")) {
                ads.sort(Comparator.comparing(o -> o.getCar().getYear()));
            } else {
                ads.sort(((o1, o2) -> o2.getCar().getYear().compareTo(o1.getCar().getYear())));
            }
        }

        if (sortPrice != null) {
            if (sortPrice.equalsIgnoreCase("acs")) {
                ads.sort(Comparator.comparing(o -> o.getPrices().getLast().getPrice()));
            } else {
                ads.sort((o1, o2) ->
                        o2.getPrices().getLast().getPrice().compareTo(o1.getPrices().getLast().getPrice()));
            }
        }

        return ResponseEntity.ok(
                AdUserAds.builder()
                        .ads(ads)
                        .build()
        );
    }

    @GetMapping("{adId}")
    @Operation(
            summary = "Получить объявление по ID",
            description = "Возвращает объявление по ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешный запрос",
                            content = @Content(schema = @Schema(implementation = AdDetailsDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Объявление не найдено"
                    )
            }
    )
    public ResponseEntity<?> getAd(@PathVariable("adId") Long adId) {
        return ResponseEntity.ok(adDetailsMapper
                .toDto(adService.findById(adId)));
    }

    @GetMapping("/getPhoto/{photoId}")
    @Operation(
            summary = "Получить фото объявления по ID",
            description = "Возвращает фото по ID",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешный запрос"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Фото не найдено"
                    )
            }
    )
    public ResponseEntity<?> getPhoto(@PathVariable("photoId") Long photoId) {
        var photo = photoService.findById(photoId);
        try {
            Path filePath = Paths.get(photo.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .headers(headers ->
                                headers.setContentDisposition(ContentDisposition
                                        .attachment()
                                        .filename(resource.getFilename(), StandardCharsets.UTF_8)
                                        .build()))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @PostMapping("/create")
    @Operation(
            summary = "Создает объявление для текущего пользователя",
            description = "Создание объявления для текущего пользователя",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные объявления",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateNewAdDto.class))
            )
            ,
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Успешный запрос",
                            content = @Content(schema = @Schema(implementation = AdDetailsDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
                    ),
                    @ApiResponse(
                            responseCode = "401"
                    )
            }
    )
    public ResponseEntity<?> createAd(@RequestPart("ad") @Valid CreateNewAdDto ad,
                                      BindingResult bindingResult,
                                      @RequestPart List<MultipartFile> files,
                                      @AuthenticationPrincipal UserDetails userDetails) {


        if (bindingResult.hasErrors()) {
            String msg = BindingResultHelper.buildFieldErrorMessage(bindingResult);
            return ResponseEntity.badRequest().body(
                    ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, msg)
            );
        }

        var user = userService.findByEmail(userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adDetailsMapper.toDto(adService.create(ad, files, user)));
    }

    @PatchMapping("/{adId}")
    @Operation(
            summary = "Изменить объявление по ID",
            description = "Изменение объявления по ID",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные объявления",
                    required = true,
                    content = @Content(schema = @Schema(implementation = EditAdDto.class))
            )
            ,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешный запрос",
                            content = @Content(schema = @Schema(implementation = AdDetailsDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
                    ),
                    @ApiResponse(
                            responseCode = "401"
                    )
            }
    )
    public ResponseEntity<?> updateAd(@PathVariable Long adId,
                                      @RequestBody @Valid EditAdDto adDto,
                                      BindingResult bindingResult, @AuthenticationPrincipal UserDetails userDetails) {

        if (bindingResult.hasErrors()) {
            var msg = BindingResultHelper.buildFieldErrorMessage(bindingResult);
            return ResponseEntity.badRequest()
                    .body(ProblemDetail.forStatusAndDetail(
                            HttpStatus.BAD_REQUEST, msg
                    ));
        }

        var ad = getAd(adId, userDetails);

        if (adDto.getPrice() != null) {
            var newPrice = Price.builder()
                    .ad(ad)
                    .price(adDto.getPrice())
                    .createdAt(LocalDateTime.now())
                    .build();
            ad.getPrices().add(newPrice);
            priceService.save(newPrice);
        }

        if (adDto.getTitle() != null)
            ad.setTitle(adDto.getTitle());

        if (adDto.getDescription() != null)
            ad.setDescription(adDto.getDescription());

        if (adDto.getIsActive() != null)
            ad.setIsActive(adDto.getIsActive());

        adService.save(ad);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private Ad getAd(Long adId, UserDetails userDetails) {
        var ad = adService.findById(adId);
        var acc = accountService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new AccountNotFoundException("Account not found!"));

        if (!ad.getUser().getAccount().getEmail().equals(userDetails.getUsername()) && acc.getRole() != Role.ROLE_ADMIN) {
            throw new AdException("Insufficient rights to do this operation");
        }
        return ad;
    }

    @DeleteMapping("/{adId}")
    @Operation(
            summary = "Удаление объявления",
            description = "Создание объявления для текущего пользователя",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешный запрос"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
                    ),
                    @ApiResponse(
                            responseCode = "401"
                    )
            }
    )
    public ResponseEntity<?> deleteAd(@PathVariable Long adId,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        getAd(adId, userDetails);

        adService.deleteById(adId);
        return ResponseEntity.ok().build();
    }

    private boolean matcherFilter(String color, String brand, String model, String yearFrom, String yearTo, String priceFrom, String priceTo, String mileageFrom, String mileageTo, String transmission, String engine, String body, String drive, Car car) {
        if (car.getAd() == null)
            return false;

        if (brand != null && !car.getBrand().getTitle().equalsIgnoreCase(brand)) {
            return false;
        }

        if (model != null && !car.getModel().getTitle().equalsIgnoreCase(model)) {
            return false;
        }

        if (color != null && !car.getColor().getTitle().equalsIgnoreCase(color)) {
            return false;
        }

        if (isValidMileage(mileageFrom, mileageTo, car))
            return false;

        if (isValidPrice(priceFrom, priceTo, car))
            return false;

        if (transmission != null && !transmission.equalsIgnoreCase(car.getTransmissionType().toString()))
            return false;

        if (engine != null && !engine.equalsIgnoreCase(car.getEngineType().toString()))
            return false;

        if (body != null && !body.equalsIgnoreCase(car.getBodyType().toString()))
            return false;

        if (drive != null && !drive.equalsIgnoreCase(car.getDrive().toString()))
            return false;


        return !isValidYear(yearFrom, yearTo, car);
    }

    private boolean isValidYear(String yearFrom, String yearTo, Car car) {
        try {
            if (yearFrom != null && yearTo != null && Integer.parseInt(yearFrom) > car.getYear() && Integer.parseInt(yearTo) < car.getYear())
                return true;

            if (yearFrom != null && Integer.parseInt(yearFrom) > car.getYear())
                return true;

            if (yearTo != null && Integer.parseInt(yearTo) < car.getYear())
                return true;

        } catch (NumberFormatException e) {
            throw new AdException("Invalid year");
        }
        return false;
    }

    private boolean isValidMileage(String mileageFrom, String mileageTo, Car car) {
        try {
            if (mileageFrom != null && mileageTo != null && Integer.parseInt(mileageFrom) > car.getMileage() && Integer.parseInt(mileageTo) < car.getMileage())
                return true;

            if (mileageFrom != null && Integer.parseInt(mileageFrom) > car.getMileage())
                return true;

            if (mileageTo != null && Integer.parseInt(mileageTo) < car.getMileage())
                return true;
        } catch (NumberFormatException e) {
            throw new AdException("Invalid mileage");
        }
        return false;
    }

    private boolean isValidPrice(String priceFrom, String priceTo, Car car) {
        try {
            if (priceFrom != null && priceTo != null &&
                Integer.parseInt(priceFrom) > car.getAd().getPrices().getLast().getPrice() &&
                Integer.parseInt(priceTo) < car.getAd().getPrices().getLast().getPrice())
                return true;

            if (priceFrom != null && Integer.parseInt(priceFrom) > car.getAd().getPrices().getLast().getPrice())
                return true;

            if (priceTo != null && Integer.parseInt(priceTo) < car.getAd().getPrices().getLast().getPrice())
                return true;
        } catch (NumberFormatException e) {
            throw new AdException("Invalid price");
        }
        return false;
    }

    @PostMapping("/addFavorite")
    public ResponseEntity<?> addFavorite(@RequestBody FavoriteDto favoriteDto, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        adService.addFavorite(favoriteDto.getUserId(), favoriteDto.getAdId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/favorites/{userId}")
    public ResponseEntity<?> favorite(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long userId) {
        if (userDetails == null || userDetails.getUsername() == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        var result = adService.getFavorites(userId)
                .stream()
                .map(adDetailsMapper::toDto)
                .toList();

        return ResponseEntity.ok().body(AdUserAds.builder()
                .ads(result)
                .build());
    }

    @DeleteMapping("/removeFavorite")
    public ResponseEntity<?> removeFavorites(@RequestBody FavoriteDto favoriteDto, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        adService.removeFavorite(favoriteDto.getUserId(), favoriteDto.getAdId());
        return ResponseEntity.ok().build();
    }


}
