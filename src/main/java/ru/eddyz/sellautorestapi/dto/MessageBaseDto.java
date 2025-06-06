package ru.eddyz.sellautorestapi.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageBaseDto {

    private Long messageId;

    private String senderName;

    @Column(length = 4096)
    private String message;

    private LocalDateTime createdAt;

}

