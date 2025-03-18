package ru.eddyz.sellautorestapi.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AdProfileDto {

    private Long adId;

    private String title;

    private String description;

    private List<PriceBaseDto> prices;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private CarProfileDto car;

    private List<ChatBaseDto> chats;
}
