package ru.eddyz.sellautorestapi.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.eddyz.sellautorestapi.dto.CreateNewAdDto;
import ru.eddyz.sellautorestapi.mapper.AdDetailsMapper;
import ru.eddyz.sellautorestapi.service.AdService;
import ru.eddyz.sellautorestapi.service.CarService;
import ru.eddyz.sellautorestapi.service.PhotoService;
import ru.eddyz.sellautorestapi.service.UserService;
import ru.eddyz.sellautorestapi.util.BindingResultHelper;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
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


    private final AdDetailsMapper adDetailsMapper;

    @GetMapping("/my")
    public ResponseEntity<?> getMyAds(@AuthenticationPrincipal UserDetails userDetails) {
        var ads = adService.findByAdsByUserEmail(userDetails.getUsername())
                .stream()
                .map(adDetailsMapper::toDto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ads);
    }

    @GetMapping
    public ResponseEntity<?> getAds(@RequestParam(name = "color", required = false) String color,
                                    @RequestParam(name = "brand", required = false) String brand,
                                    @RequestParam(name = "model", required = false) String model) {

        if (color != null && brand != null && model != null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(
                            carService.findByBrandTitleAndModelTitleAndColorTitle(
                                            brand, model, color
                                    ).stream()
                                    .map(car -> adDetailsMapper.toDto(car.getAd()))
                                    .toList()
                    );
        }

        var ads = carService.findAll()
                .stream()
                .filter(car -> {
                    System.out.println(car.getBrand().getTitle());
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

                    return true;
                }).map(car -> adDetailsMapper.toDto(car.getAd()))
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

    @SneakyThrows
    @PostMapping(value = "/create", consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE,
    })
    public ResponseEntity<?> createAd(@RequestPart("ad") String add,
                                      BindingResult bindingResult,
                                      @RequestPart("photos") List<MultipartFile> photos,
                                      @AuthenticationPrincipal UserDetails userDetails) {

        var ad = objectMapper.readValue(add, CreateNewAdDto.class);
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

}
