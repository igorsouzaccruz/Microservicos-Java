package com.microservico.product.controllers.interfaces;

import com.microservico.product.models.dtos.ProductDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@Tag(name = "Products", description = "Endpoints para Gerenciamento de Produtos")
@RequestMapping("/api/products")
public interface IProductController {

    @Operation(
            summary = "Listar todos os produtos",
            description = "Retorna uma lista com todos os produtos cadastrados."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Operação bem-sucedida",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProductDTO.class))
    )
    @GetMapping
    List<ProductDTO> list();

    @Operation(
            summary = "Buscar produto por ID",
            description = "Retorna um único produto com base no seu ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDTO.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    ProductDTO findById(
            @Parameter(description = "ID do produto a ser buscado", required = true, example = "1")
            @PathVariable("id") @NotNull @Positive Long id
    );

    @Operation(
            summary = "Criar um novo produto",
            description = "Cria um novo produto e retorna os dados do produto criado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Produto criado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content)
    })
    @PostMapping
    @ResponseStatus(code = org.springframework.http.HttpStatus.CREATED)
    ProductDTO create(
            @Parameter(description = "Objeto do produto a ser criado", required = true)
            @RequestBody @Valid @NotNull ProductDTO product
    );

    @Operation(
            summary = "Atualizar um produto existente",
            description = "Atualiza os dados de um produto existente com base no seu ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDTO.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content)
    })
    @PutMapping("/{id}")
    ProductDTO update(
            @Parameter(description = "ID do produto a ser atualizado", required = true, example = "1")
            @PathVariable("id") @NotNull @Positive Long id,

            @Parameter(description = "Objeto do produto com os dados atualizados", required = true)
            @RequestBody @Valid @NotNull ProductDTO product
    );

    @Operation(
            summary = "Deletar um produto",
            description = "Exclui um produto com base no seu ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Produto excluído com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(code = org.springframework.http.HttpStatus.NO_CONTENT)
    void delete(
            @Parameter(description = "ID do produto a ser excluído", required = true, example = "1")
            @PathVariable("id") @NotNull @Positive Long id
    );
}
