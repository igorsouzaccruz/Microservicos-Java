package com.microservico.sales.controllers.interfaces;


import com.microservico.sales.models.dtos.SaleRequest;
import com.microservico.sales.models.dtos.SaleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Sales API",
        description = "Gerencia operações relacionadas às vendas realizadas pelos usuários."
)
@RequestMapping("/sales")
public interface ISaleControllerDocs {

    @Operation(
            summary = "Cria uma nova venda",
            description = """
                    Cria um novo registro de venda com base nas informações fornecidas.  
                    O cabeçalho `X-User-Id` é obrigatório e identifica o usuário responsável pela venda.
                    """,
            requestBody = @RequestBody(
                    required = true,
                    description = "Objeto contendo os detalhes da venda a ser criada.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SaleRequest.class)
                    )
            ),
            parameters = {
                    @Parameter(
                            name = "X-User-Id",
                            description = "Identificador do usuário que está realizando a venda.",
                            required = true,
                            in = ParameterIn.HEADER,
                            example = "10"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Venda criada com sucesso.",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SaleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Requisição inválida (dados ausentes ou incorretos).",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Produto não encontrado no serviço de produtos.",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Erro interno no servidor.",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    )
            }
    )
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    SaleResponse create(
            @Valid @org.springframework.web.bind.annotation.RequestBody SaleRequest request,
            @NotNull @Positive @RequestHeader(value = "X-User-Id", required = true) String userIdHeader
    );

    @Operation(
            summary = "Lista todas as vendas de um usuário",
            description = "Retorna todas as vendas associadas ao identificador do usuário informado.",
            parameters = {
                    @Parameter(
                            name = "userId",
                            description = "Identificador único do usuário.",
                            required = true,
                            example = "10"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de vendas retornada com sucesso.",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SaleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Nenhuma venda encontrada para o usuário informado.",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    )
            }
    )
    @GetMapping(value = "/user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    List<SaleResponse> listByUser(@PathVariable Long userId);
}