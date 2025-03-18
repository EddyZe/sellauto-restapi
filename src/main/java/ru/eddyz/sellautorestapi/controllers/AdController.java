package ru.eddyz.sellautorestapi.controllers;



import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.eddyz.sellautorestapi.mapper.AdDetailsMapper;
import ru.eddyz.sellautorestapi.service.AdService;
import ru.eddyz.sellautorestapi.service.CarService;
import ru.eddyz.sellautorestapi.service.PhotoService;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ads")
public class AdController {

    private final AdService adService;
    private final CarService carService;
    private final PhotoService photoService;

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
}
