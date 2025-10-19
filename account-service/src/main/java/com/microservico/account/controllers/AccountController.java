package com.microservico.account.controllers;

import com.microservico.account.models.dto.LoginDTO;
import com.microservico.account.models.dto.RegisterDTO;
import com.microservico.account.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AuthService authService;

    public AccountController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Validated RegisterDTO dto) {
        authService.register(dto);
        return ResponseEntity.status(201).body(Map.of("message", "Usuário registrado com sucesso"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Validated LoginDTO dto) {
        String token = authService.login(dto);
        return ResponseEntity.ok(Map.of("access_token", token));
    }
}
