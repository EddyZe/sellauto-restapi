package ru.eddyz.sellautorestapi.controllers;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.eddyz.sellautorestapi.dto.AuthLoginDto;
import ru.eddyz.sellautorestapi.dto.CreateAccountDto;
import ru.eddyz.sellautorestapi.entities.Account;
import ru.eddyz.sellautorestapi.entities.RefreshToken;
import ru.eddyz.sellautorestapi.enums.Role;
import ru.eddyz.sellautorestapi.exeptions.AccountException;
import ru.eddyz.sellautorestapi.exeptions.AccountNotFoundException;
import ru.eddyz.sellautorestapi.mapper.UserMapper;
import ru.eddyz.sellautorestapi.security.JwtService;
import ru.eddyz.sellautorestapi.service.AccountService;
import ru.eddyz.sellautorestapi.service.RefreshTokenService;
import ru.eddyz.sellautorestapi.service.UserService;
import ru.eddyz.sellautorestapi.util.BindingResultHelper;

import java.time.Duration;
import java.util.Date;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AccountService accountService;
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;


    @PostMapping("/sing-up")
    public ResponseEntity<?> singUp(@RequestBody @Valid CreateAccountDto createUserDto,
                                    BindingResult bindingResult) {


        if (bindingResult.hasErrors()) {
            var msg = BindingResultHelper.buildFieldErrorMessage(bindingResult);

            return ResponseEntity.badRequest().body(
                    ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, msg)
            );
        }

        var user = userService.save(createUserDto.getFirstName(), createUserDto.getLastName());
        var acc = accountService.createAccount(Account.builder()
                .role(Role.ROLE_USER)
                .email(createUserDto.getEmail())
                .user(user)
                .phoneNumber(createUserDto.getPhoneNumber())
                .password(createUserDto.getPassword())
                .blocked(false)
                .build());
        user.setAccount(acc);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userMapper.toDto(user));
    }

    @PostMapping(value = "/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthLoginDto authLoginDto,
                                   BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            var msg = BindingResultHelper.buildFieldErrorMessage(bindingResult);
            return ResponseEntity.badRequest().body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, msg));
        }

        var account = accountService.findByEmail(authLoginDto.getEmail())
                .orElseThrow(() -> new AccountException("Invalid email or password", "ACCOUNT_NOT_FOUND"));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authLoginDto.getEmail(),
                        authLoginDto.getPassword()
                )
        );

        Date currentDate = new Date(System.currentTimeMillis());
        var token = jwtService.generateToken(accountService.buildUserDetails(account),
                new Date(currentDate.getTime() + Duration.ofMinutes(15).toMillis()));
        var expiredDateRefreshToken = currentDate.getTime() + Duration.ofDays(30).toMillis();
        var refreshToken = jwtService.generateToken(accountService.buildUserDetails(account),
                new Date(expiredDateRefreshToken));
        refreshTokenService.save(RefreshToken.builder()
                .account(account)
                .blocked(false)
                .expiredDate(new Date(expiredDateRefreshToken))
                .token(refreshToken)
                .build());
        return ResponseEntity.status(HttpStatus.OK).body(
                Map.of(
                        "accessToken", token,
                        "refreshToken", refreshToken
                )
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshTokens(@RequestHeader("Authorization") String refreshTokenHeader) {
        var refreshToken = refreshTokenHeader.substring("Bearer ".length());
        var email = jwtService.extractEmail(refreshToken);

        var user = accountService.loadUserByUsername(email);

        if (!jwtService.validateToken(refreshToken, user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid refresh token"));
        }

        var account = accountService.findByEmail(jwtService.extractEmail(refreshToken))
                .orElseThrow(() -> new AccountNotFoundException("account not found"));

        Date currentDate = new Date(System.currentTimeMillis());
        String newAccessToken = jwtService.generateToken(accountService.buildUserDetails(account),
                new Date(currentDate.getTime() + Duration.ofMinutes(15).toMillis()));

        long expiredDateRefreshToken = currentDate.getTime() + Duration.ofDays(30).toMillis();
        String newRefreshToken = jwtService.generateToken(accountService.buildUserDetails(account),
                new Date(expiredDateRefreshToken));

        refreshTokenService.blockedRefreshToken(refreshToken);

        refreshTokenService.save(RefreshToken.builder()
                .account(account)
                .blocked(false)
                .expiredDate(new Date(expiredDateRefreshToken))
                .token(newRefreshToken)
                .build());


        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken));
    }
}
