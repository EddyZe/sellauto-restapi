package ru.eddyz.sellautorestapi.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.eddyz.sellautorestapi.entities.Ad;
import ru.eddyz.sellautorestapi.entities.Chat;
import ru.eddyz.sellautorestapi.entities.Message;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {

    private Long userId;

    private String firstName;

    private String lastName;

    private AccountDto account;

    private List<AdDto> ads;

//    @ManyToMany(mappedBy = "users")
//    private List<Chat> chats;

//    @OneToMany(mappedBy = "from")
//    private List<Message> sendMessages;

}
