package ru.eddyz.sellautorestapi.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.eddyz.sellautorestapi.enums.BodyType;
import ru.eddyz.sellautorestapi.enums.DriveMode;
import ru.eddyz.sellautorestapi.enums.EngineType;
import ru.eddyz.sellautorestapi.enums.TransmissionType;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateNewAdDto {

    @Min(1)
    @NotNull
    private Double price;
    @NotEmpty
    private String title;
    @NotEmpty
    @Size(min = 5)
    private String description;

    @Min(1900)
    @NotNull
    private Integer year;

    @NotEmpty
    private String vin;

    @Min(1)
    @NotNull
    private Integer mileage;

    @NotNull
    private EngineType engineType;

    @NotNull
    private TransmissionType transmissionType;

    @NotNull
    private BodyType bodyType;

    @NotNull
    private DriveMode drive;

    @NotEmpty
    private String brandTitle;

    @NotEmpty
    private String modelTitle;

    @NotEmpty
    private String colorTitle;
}
