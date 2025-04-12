package ru.eddyz.sellautorestapi.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.eddyz.sellautorestapi.dto.EditProfileDto;
import ru.eddyz.sellautorestapi.dto.ProfilesDto;
import ru.eddyz.sellautorestapi.dto.UserProfileDto;
import ru.eddyz.sellautorestapi.exeptions.AccountException;
import ru.eddyz.sellautorestapi.exeptions.AccountNotFoundException;
import ru.eddyz.sellautorestapi.mapper.UserMapper;
import ru.eddyz.sellautorestapi.service.AccountService;
import ru.eddyz.sellautorestapi.service.UserService;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@Tag(name = "Профиль пользователя")
public class ProfileController {


    private final AccountService accountService;
    private final UserMapper userMapper;
    private final UserService userService;

    @GetMapping("/my")
    @Operation(
            summary = "Профиль текущего пользователя",
            description = "Открывает профиль текущего пользователя",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = UserProfileDto.class))
                    ),
                    @ApiResponse(
                            description = "Ошибка",
                            content = @Content(schema = @Schema(implementation = ProfilesDto.class))
                    )
            }
    )
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        var acc = accountService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new AccountNotFoundException("Not found"));
        return ResponseEntity.status(HttpStatus.OK)
                .body(userMapper.toDto(acc.getUser()));
    }

    @GetMapping("/{userId}")
    @Operation(
            summary = "Получить профиль по ID",
            description = "Возвращает профиль пользователя по указанному ID",
            parameters = {
                    @Parameter(name = "userId", description = "ID пользователя")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = UserProfileDto.class))
                    ),
                    @ApiResponse(
                            description = "Ошибка",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
                    )
            }
    )
    public ResponseEntity<?> getUser(@PathVariable Long userId) {
        return ResponseEntity
                .ok(userMapper.toDto(userService.findById(userId)));
    }

    @GetMapping
    @Operation(
            summary = "Получить список всех пользователей",
            description = "Позволяет получить список пользователей",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = ProfilesDto.class))
                    )
            }
    )
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.ok(
                ProfilesDto.builder()
                        .profiles(userService.findAll()
                                .stream()
                                .map(userMapper::toDto)
                                .toList())
                        .build()
        );
    }

    @PatchMapping
    @Operation(
            summary = "Изменить данные пользователя",
            description = "Позволяет изменить данные текущего пользователя",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новые данные пользователя",
                    content = @Content(schema = @Schema(implementation = EditProfileDto.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "204"),
                    @ApiResponse(
                            description = "Ошибка",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
                    )
            }
    )
    public ResponseEntity<?> editProfile(@AuthenticationPrincipal UserDetails userDetails,
                                         @RequestBody @Valid EditProfileDto editProfileDto) {

        var acc = accountService.findByEmail(userDetails.getUsername()).
                orElseThrow(() -> new AccountNotFoundException("Not found"));
        var user = acc.getUser();
        var accOp = accountService.findByPhoneNumber(editProfileDto.getPhoneNumber());

        if (editProfileDto.getPhoneNumber() != null && accOp.isPresent() &&
            !user.getAccount().getPhoneNumber().equals(editProfileDto.getPhoneNumber())) {
            throw new AccountException("Phone number already exist", "PHONE_NUMBER_IS_EXIST");
        }

        var firstName = editProfileDto.getFirstName() == null ? user.getFirstName() : editProfileDto.getFirstName();
        var lastName = editProfileDto.getLastName() == null ? user.getLastName() : editProfileDto.getLastName();
        var phoneNumber = editProfileDto.getPhoneNumber() == null ? acc.getPhoneNumber() : editProfileDto.getPhoneNumber();

        acc.setPhoneNumber(phoneNumber);
        user.setFirstName(firstName);
        user.setLastName(lastName);

        accountService.update(acc);
        user.setAccount(acc);
        userService.update(user);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
