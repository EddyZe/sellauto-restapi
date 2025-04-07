package ru.eddyz.sellautorestapi.dto;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateAccountDto {
    @NotNull
    @NotBlank
    @Email
    private String email;

    @NotNull
    @NotBlank
    private String phoneNumber;

    @NotBlank
    @NotNull
    @Size(min = 6, max = 20)
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z]).+$", message = "The password must contain at least one uppercase and lowercase letter")
    private String password;

    @NotBlank
    @Size(min = 2, max = 30)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 30)
    private String lastName;
}
