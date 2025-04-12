package ru.eddyz.sellautorestapi.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ResponseOkAuth {

    private String accessToken;
    private String refreshToken;
}
