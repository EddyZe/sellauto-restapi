package ru.eddyz.sellautorestapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;




@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdDetailsDto {
    private Long adId;

    private String title;

    private String description;

    private List<PriceBaseDto> prices;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private UserBaseDto user;

    private CarDetailsDto car;


//    private List<ChatBaseDto> chats;
}
