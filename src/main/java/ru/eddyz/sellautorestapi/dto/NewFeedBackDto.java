package ru.eddyz.sellautorestapi.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class NewFeedBackDto {

    @NotNull
    private Long receiverId;
    @NotNull
    @Min(1)
    @Max(5)
    private Integer estimation;
    private String text;

}
