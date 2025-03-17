package ru.eddyz.sellautorestapi.controllers;


import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Reader;

@RestController
public class TestController {



    @GetMapping("test")
    private String test(@AuthenticationPrincipal UserDetails user) {
        return user.getUsername();
    }
}
