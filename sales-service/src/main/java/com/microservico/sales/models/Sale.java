package com.microservico.sales.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "sales")
public class Sale implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Positive
    private Long productId;
    @NotNull
    @Positive
    private Long userId;
    private Boolean ativo;
    @NotNull
    @Positive
    private Integer quantity;
    private LocalDateTime saleDate;

    public Sale() {
    }

    public Sale(Long id, Long productId, Long userId, Boolean ativo, Integer quantity, LocalDateTime saleDate) {
        this.id = id;
        this.productId = productId;
        this.userId = userId;
        this.ativo = ativo;
        this.quantity = quantity;
        this.saleDate = saleDate;
    }

    public Sale(Long productId, Long userId, Boolean ativo, Integer quantity, LocalDateTime saleDate) {
        this.productId = productId;
        this.userId = userId;
        this.ativo = ativo;
        this.quantity = quantity;
        this.saleDate = saleDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Sale sale = (Sale) o;
        return Objects.equals(id, sale.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Sale{" +
                "id=" + id +
                ", productId=" + productId +
                ", userId=" + userId +
                ", ativo=" + ativo +
                ", quantity=" + quantity +
                ", saleDate=" + saleDate +
                '}';
    }
}
