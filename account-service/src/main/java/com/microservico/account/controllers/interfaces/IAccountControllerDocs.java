package com.microservico.account.controllers.interfaces;

import com.microservico.account.models.dto.LoginDTO;
import com.microservico.account.models.dto.RegisterDTO;
import com.microservico.account.models.dto.RegisterResponseDTO;
import com.microservico.account.models.dto.TokenDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(
        name = "Account",
        description = "Endpoints de autenticação e cadastro de usuários"
)
public interface IAccountControllerDocs {
    @Operation(
            summary = "Registrar um novo usuário",
            description = "Cria uma nova conta de usuário no sistema e retorna uma mensagem de sucesso."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Usuário registrado com sucesso",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RegisterResponseDTO.class),
                    examples = @ExampleObject(value = "{ \"message\": \"Usuário registrado com sucesso\" }")
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Dados de entrada inválidos",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = "{ \"error\": \"Email inválido\" }")
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Usuário já existente",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = "{ \"error\": \"Email já cadastrado\" }")
            )
    )
    @PostMapping("/register")
    ResponseEntity<RegisterResponseDTO> register(
            @RequestBody @Validated RegisterDTO dto
    );

    @Operation(
            summary = "Autenticar um usuário",
            description = "Realiza login de um usuário existente e retorna um token JWT válido."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Login realizado com sucesso",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TokenDTO.class),
                    examples = @ExampleObject(value = "{ \"access_token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\" }")
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "Credenciais inválidas",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = "{ \"error\": \"Usuário ou senha incorretos\" }")
            )
    )
    @PostMapping("/login")
    ResponseEntity<TokenDTO> login(
            @RequestBody @Validated LoginDTO dto
    );
}