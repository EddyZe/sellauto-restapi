package ru.eddyz.sellautorestapi.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.eddyz.sellautorestapi.dto.CreateNewAdDto;
import ru.eddyz.sellautorestapi.entities.*;
import ru.eddyz.sellautorestapi.exeptions.AdNotFountException;
import ru.eddyz.sellautorestapi.exeptions.ServerException;
import ru.eddyz.sellautorestapi.repositories.AdRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class AdService {

    private final AdRepository adRepository;
    private final BrandService brandService;
    private final ColorService colorService;
    private final ModelService modelService;
    private final PriceService priceService;
    private final PhotoService photoService;
    private final CarService carService;

    @Value("${ad.photo.directory}")
    private String photoFilePath;


    public List<Ad> findUserAdsByUserId(Long userId) {
        return adRepository.findAdUser(userId);
    }

    public List<Ad> findByAdsByUserEmail(String email) {
        return adRepository.findAdUserByEmail(email);
    }


    @Transactional
    public Ad findById(Long adId) {
        return adRepository.findById(adId)
                .orElseThrow(() -> new AdNotFountException("Ad not  found"));
    }

    public void deleteById(Long adId) {
        adRepository.deleteById(adId);
    }

    public Ad save(Ad ad) {
        return adRepository.save(ad);
    }


    @Transactional
    public Ad create(CreateNewAdDto createNewAdDto, List<MultipartFile> photos, User user) {
        var color = colorService.findByTitle(createNewAdDto.getColorTitle());
        var brand = brandService.findByTitle(createNewAdDto.getBrandTitle());
        var model = modelService.findByTitleAndBrandTitle(createNewAdDto.getModelTitle(), createNewAdDto.getBrandTitle());

        var ad = buildNewAd(createNewAdDto, user);
        ad = adRepository.save(ad);

        var car = buildNwCar(createNewAdDto, brand, model, color, ad);
        car = carService.save(car);

        try {
            for (String path : savePhotos(photos)) {
                var photo = photoService.save(Photo.builder()
                        .car(car)
                        .filePath(path)
                        .build());

                if (car.getPhotos() == null) {
                    car.setPhotos(new ArrayList<>());
                }

                car.getPhotos().add(photo);
            }
        } catch (IOException e) {
            log.error("error saving photo {}", e.getMessage());
            throw new ServerException("error creating ad", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        var price = priceService.save(Price.builder()
                .createdAt(LocalDateTime.now())
                .price(createNewAdDto.getPrice())
                .ad(ad)
                .build());

        ad.setPrices(Collections.singletonList(price));
        ad.setCar(car);

        return ad;
    }

    private List<String> savePhotos(List<MultipartFile> photos) throws IOException {
        var dir = Paths.get(photoFilePath).normalize();

        if (!Files.exists(dir)) {
            Files.createDirectory(dir);
        }

        List<String> photoPaths = new ArrayList<>();

        for (MultipartFile photo : photos) {
            var fileName = UUID.randomUUID() + "_" + photo.getOriginalFilename();
            var filePath = dir.resolve(fileName);
            Files.copy(photo.getInputStream(), filePath);
            photoPaths.add(filePath.toString());
        }

        return photoPaths;
    }

    private Car buildNwCar(CreateNewAdDto createNewAdDto, Brand brand, Model model, Color color, Ad ad) {
        return Car.builder()
                .brand(brand)
                .model(model)
                .color(color)
                .bodyType(createNewAdDto.getBodyType())
                .mileage(createNewAdDto.getMileage())
                .year(createNewAdDto.getYear())
                .vin(createNewAdDto.getVin())
                .transmissionType(createNewAdDto.getTransmissionType())
                .drive(createNewAdDto.getDrive())
                .engineType(createNewAdDto.getEngineType())
                .ad(ad)
                .build();
    }

    private Ad buildNewAd(CreateNewAdDto createNewAdDto, User user) {
        return Ad.builder()
                .createdAt(LocalDateTime.now())
                .title(createNewAdDto.getTitle())
                .description(createNewAdDto.getDescription())
                .isActive(true)
                .user(user)
                .build();
    }

    public void addFavorite(Long userId, Long adId) {
        if (adRepository.findById(adId).isPresent()) {
            adRepository.addFavorite(userId, adId);
        } else {
            throw new AdNotFountException("Ad not found");
        }
    }

    public void removeFavorite(Long userId, Long adId) {
        adRepository.deleteFavorite(userId, adId);
    }

    public List<Ad> getFavorites(Long userId) {
        return adRepository.favoriteAdByUserId(userId);
    }
}
