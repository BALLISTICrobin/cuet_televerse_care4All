package com.careforall.auth_service.controller;

import com.careforall.auth_service.model.UserCredential;
import com.careforall.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/register")
    public String addNewUser(@RequestBody UserCredential user) {
        return service.saveUser(user);
    }

    @PostMapping("/token")
    public String getToken(@RequestBody UserCredential user) {
        // Simplified login: Just generates token for given username without checking password
        // (Add AuthenticationManager check for production)
        return service.generateToken(user.getUsername());
    }
}