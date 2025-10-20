package com.microservico.account.controllers;

import com.microservico.account.controllers.interfaces.IAccountControllerDocs;
import com.microservico.account.models.dto.LoginDTO;
import com.microservico.account.models.dto.RegisterDTO;
import com.microservico.account.models.dto.RegisterResponseDTO;
import com.microservico.account.models.dto.TokenDTO;
import com.microservico.account.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountController implements IAccountControllerDocs {

    private final AuthService authService;

    public AccountController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@RequestBody @Validated RegisterDTO dto) {
        authService.register(dto);
        var response = new RegisterResponseDTO("Usuário registrado com sucesso");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@RequestBody @Validated LoginDTO dto) {
        String token = authService.login(dto);
        var tokenDto = new TokenDTO(token);
        return ResponseEntity.ok(tokenDto);
    }
}
