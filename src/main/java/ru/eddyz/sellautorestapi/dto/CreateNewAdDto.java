package ru.eddyz.sellautorestapi.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(allowableValues = {"ELECTRO", "ENGINE"}, description = "Тип двигателя", example = "ELECTRO")
    private EngineType engineType;

    @NotNull
    @Schema(allowableValues = {"AUTO", "MECHANIC", "ROBOT", "VARIATOR"}, description = "Тип коробки передач", example = "ROBOT")
    private TransmissionType transmissionType;

    @NotNull
    @Schema(
            allowableValues = {"SEDAN", "HATCHBACK", "UNIVERSAL", "COUPE", "PICKUP", "SUV", "MINIVAN", "OTHER"},
            description = "Тип кузова",
            example = "SEDAN"
    )
    private BodyType bodyType;

    @NotNull
    @Schema(
            allowableValues = {"AWD", "FRONT", "REAR"},
            description = "Привод",
            example = "AWD"
    )
    private DriveMode drive;

    @NotEmpty
    private String brandTitle;

    @NotEmpty
    private String modelTitle;

    @NotEmpty
    private String colorTitle;
}
