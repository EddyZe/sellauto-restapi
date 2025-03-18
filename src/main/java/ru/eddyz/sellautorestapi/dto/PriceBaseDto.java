package ru.eddyz.sellautorestapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;




@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PriceBaseDto {
    private Integer priceId;

    private Double price;

    private LocalDateTime createdAt;
}
