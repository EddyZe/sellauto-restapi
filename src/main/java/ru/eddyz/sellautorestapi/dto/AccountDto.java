package ru.eddyz.sellautorestapi.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.eddyz.sellautorestapi.enums.Role;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountDto {

    private Long accountId;

    private String email;

    private String phoneNumber;

    @JsonProperty(access =  JsonProperty.Access.WRITE_ONLY)
    private String password;

    private boolean blocked;

    private Role role;

}
