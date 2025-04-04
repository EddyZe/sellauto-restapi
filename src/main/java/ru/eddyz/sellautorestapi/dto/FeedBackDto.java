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
public class FeedBackDto {
    private Long id;

    private Double estimation;

    private String text;

    private UserBaseDto sender;

    private UserBaseDto receiver;

    private LocalDateTime createdAt;
}
