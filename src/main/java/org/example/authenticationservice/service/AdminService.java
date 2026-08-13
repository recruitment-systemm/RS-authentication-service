package org.example.authenticationservice.service;

import lombok.RequiredArgsConstructor;
import org.example.authenticationservice.config.Properties.AdminProperties;
import org.example.authenticationservice.dto.request.AdminLoginRequest;
import org.example.authenticationservice.exceptions.InvalidCredentialsException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminProperties adminProperties;
    public void login(AdminLoginRequest request) {
        if (!adminProperties.getEmail().equals(request.email()) || !adminProperties.getPassword().equals(request.password())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }
}