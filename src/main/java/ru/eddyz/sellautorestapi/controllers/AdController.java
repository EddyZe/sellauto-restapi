package ru.eddyz.sellautorestapi.controllers;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.eddyz.sellautorestapi.dto.CreateNewAdDto;
import ru.eddyz.sellautorestapi.dto.EditAdDto;
import ru.eddyz.sellautorestapi.entities.Car;
import ru.eddyz.sellautorestapi.entities.Price;
import ru.eddyz.sellautorestapi.enums.Role;
import ru.eddyz.sellautorestapi.exeptions.AccountNotFoundException;
import ru.eddyz.sellautorestapi.exeptions.AdException;
import ru.eddyz.sellautorestapi.mapper.AdDetailsMapper;
import ru.eddyz.sellautorestapi.service.*;
import ru.eddyz.sellautorestapi.util.BindingResultHelper;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ads")
public class AdController {

    private final AdService adService;
    private final CarService carService;
    private final PhotoService photoService;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final AccountService accountService;


    private final AdDetailsMapper adDetailsMapper;

    @GetMapping("/my")
    public ResponseEntity<?> getMyAds(@AuthenticationPrincipal UserDetails userDetails) {
        var ads = adService.findByAdsByUserEmail(userDetails.getUsername())
                .stream()
                .map(adDetailsMapper::toDto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ads);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getAdUser(@PathVariable("userId") Long userId) {
        var user = userService.findById(userId);
        return ResponseEntity.ok(
                adService.findByAdsByUserEmail(user.getAccount().getEmail())
        );
    }

    @GetMapping
    public ResponseEntity<?> getAds(@RequestParam(name = "color", required = false) String color,
                                    @RequestParam(name = "brand", required = false) String brand,
                                    @RequestParam(name = "model", required = false) String model,
                                    @RequestParam(name = "year-from", required = false) String yearFrom,
                                    @RequestParam(name = "year-to", required = false) String yearTo) {
        var ads = carService.findAll()
                .stream()
                .filter(car -> matcherFilter(color, brand, model, yearFrom, yearTo, car))
                .map(car -> adDetailsMapper.toDto(car.getAd()))
                .toList();

        return ResponseEntity.status(HttpStatus.OK)
                .body(ads);
    }

    @GetMapping("/getPhoto/{photoId}")
    public ResponseEntity<?> getPhoto(@PathVariable("photoId") Long photoId) {
        var photo = photoService.findById(photoId);
        try {
            Path filePath = Paths.get(photo.getFilePath()).normalize();
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

    @PostMapping(value = "/create", consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE,
    })
    public ResponseEntity<?> createAd(@RequestPart("ad") String add,
                                      BindingResult bindingResult,
                                      @RequestPart("photos") List<MultipartFile> photos,
                                      @AuthenticationPrincipal UserDetails userDetails) {

        CreateNewAdDto ad = convertJsonToAdDto(add);
        try {
            validator.validate(ad, bindingResult);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ResponseEntity.badRequest()
                            .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage()))
            );
        }

        if (bindingResult.hasErrors()) {
            String msg = BindingResultHelper.buildFieldErrorMessage(bindingResult);
            return ResponseEntity.badRequest().body(
                    ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, msg)
            );
        }

        var user = userService.findByEmail(userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adDetailsMapper.toDto(adService.create(ad, photos, user)));
    }

    @PatchMapping("/{adId}")
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

        var ad = adService.findById(adId);
        var acc = accountService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new AccountNotFoundException("Account not found!"));

        if (!ad.getUser().getAccount().getEmail().equals(userDetails.getUsername()) || acc.getRole() != Role.ROLE_ADMIN) {
            throw new AdException("Insufficient rights to do this operation");
        }

        ad.getPrices().add(Price.builder()
                .ad(ad)
                .price(adDto.getPrice())
                .createdAt(LocalDateTime.now())
                .build());

        ad.setTitle(adDto.getTitle());
        ad.setDescription(adDto.getDescription());
        adService.save(ad);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{adId}")
    public ResponseEntity<?> deleteAd(@PathVariable Long adId,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        var ad = adService.findById(adId);
        var acc = accountService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new AccountNotFoundException("Account not found!"));

        if (!ad.getUser().getAccount().getEmail().equals(userDetails.getUsername()) || acc.getRole() != Role.ROLE_ADMIN) {
            throw new AdException("Insufficient rights to do this operation");
        }

        adService.deleteById(adId);
        return ResponseEntity.ok().build();
    }

    private CreateNewAdDto convertJsonToAdDto(String add) {

        try {
            return objectMapper.readValue(add, CreateNewAdDto.class);
        } catch (JsonProcessingException e) {
            throw new AdException("invalid JSON");
        }
    }

    private boolean matcherFilter(String color, String brand, String model, String yearFrom, String yearTo, Car car) {
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

        return !isValidYear(yearFrom, yearTo, car);
    }

    private boolean isValidYear(String yearFrom, String yearTo, Car car) {
        try {
            if (yearFrom != null && Integer.parseInt(yearFrom) < car.getYear())
                return true;

            if (yearTo != null && Integer.parseInt(yearTo) > car.getYear())
                return true;
        } catch (NumberFormatException e) {
            throw new AdException("Invalid year");
        }
        return false;
    }


}
