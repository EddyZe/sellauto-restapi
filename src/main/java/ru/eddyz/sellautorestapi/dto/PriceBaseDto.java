package ru.eddyz.sellautorestapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
