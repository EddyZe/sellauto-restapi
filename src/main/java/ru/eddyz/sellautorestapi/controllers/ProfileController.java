package ru.eddyz.sellautorestapi.controllers;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.eddyz.sellautorestapi.dto.EditProfileDto;
import ru.eddyz.sellautorestapi.dto.UserDto;
import ru.eddyz.sellautorestapi.exeptions.AccountException;
import ru.eddyz.sellautorestapi.exeptions.AccountNotFoundException;
import ru.eddyz.sellautorestapi.mapper.UserMapper;
import ru.eddyz.sellautorestapi.service.AccountService;
import ru.eddyz.sellautorestapi.service.UserService;
import ru.eddyz.sellautorestapi.util.BindingResultHelper;

import java.util.Collections;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {


    private final AccountService accountService;
    private final UserMapper userMapper;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userMapper.toDto(accountService.findByEmail(userDetails.getUsername())
                        .orElseThrow(() -> new AccountNotFoundException("Not found"))
                        .getUser()));
    }

    @PatchMapping
    public ResponseEntity<?> editProfile(@AuthenticationPrincipal UserDetails userDetails,
                                         @RequestBody @Valid EditProfileDto editProfileDto) {

        var acc = accountService.findByEmail(userDetails.getUsername()).
                orElseThrow(() -> new AccountNotFoundException("Not found"));
        var user = acc.getUser();

        if (editProfileDto.getPhoneNumber() != null && accountService.findByPhoneNumber(editProfileDto.getPhoneNumber()).isPresent()) {
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
