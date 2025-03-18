package ru.eddyz.sellautorestapi.dto;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.eddyz.sellautorestapi.entities.Brand;
import ru.eddyz.sellautorestapi.entities.Color;
import ru.eddyz.sellautorestapi.entities.Model;
import ru.eddyz.sellautorestapi.entities.Photo;
import ru.eddyz.sellautorestapi.enums.BodyType;
import ru.eddyz.sellautorestapi.enums.DriveMode;
import ru.eddyz.sellautorestapi.enums.EngineType;
import ru.eddyz.sellautorestapi.enums.TransmissionType;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CarDetailsDto {
    private Long carId;

    private Integer year;

    private String vin;

    private Integer mileage;

    private EngineType engineType;

    private TransmissionType transmissionType;

    private BodyType bodyType;

    private DriveMode drive;

    private BrandBaseDto brand;

    private ModelBaseDto model;

    private List<PhotoBaseDto> photos;

    private ColorBaseDto color;
}
