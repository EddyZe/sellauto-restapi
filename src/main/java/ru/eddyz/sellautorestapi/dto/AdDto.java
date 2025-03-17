package ru.eddyz.sellautorestapi.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;



@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AdDto {

    private Long adId;

    private String title;

    private String description;

//    private List<Price> prices;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private UserDto user;


//    private Car car;

//    @OneToMany(mappedBy = "ad", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
//    private List<Chat> chats;
}
