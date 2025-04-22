package ru.eddyz.sellautorestapi.dto;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ResetPasswordDto {

    @NotNull
    @NotEmpty
    @Email
    private String email;
    @NotEmpty
    @Size(min = 6)
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z]).+$", message = "The password must contain at least one uppercase and lowercase letter")
    private String password;
    @NotEmpty
    private String code;
}
