package com.careforall.auth_service.service;

import com.careforall.auth_service.model.UserCredential;
import com.careforall.auth_service.repository.UserCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserCredentialRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String saveUser(UserCredential credential) {
        credential.setPassword(passwordEncoder.encode(credential.getPassword()));
        repository.save(credential);
        return "User added to system";
    }

    public String generateToken(String username) {
        return jwtService.generateToken(username);
    }

    public void validateToken(String token) {
        // In a real app, decode and check expiry here.
        // For now, we trust the signature check in Gateway logic if implemented.
        // Or implement Jwts.parserBuilder()...parseClaimsJws(token)
    }
}