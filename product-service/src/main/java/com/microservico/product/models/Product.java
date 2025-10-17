package com.microservico.product.models;

import com.microservico.product.models.enums.CategoryEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "products")
public class Product implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @NotNull
    @Length(max = 150)
    @Column(length = 150, nullable = false)
    private String description;

    @NotNull
    private CategoryEnum category;

    @Positive
    @NotNull
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    public Product() {
    }

    public Product(String description, CategoryEnum category, BigDecimal price) {
        this.description = description;
        this.category = category;
        this.price = price;
    }

    public Product(Long id, String description, CategoryEnum category, BigDecimal price) {
        this.id = id;
        this.description = description;
        this.category = category;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotBlank @NotNull @Length(max = 150) String getDescription() {
        return description;
    }

    public void setDescription(@NotBlank @NotNull @Length(max = 150) String description) {
        this.description = description;
    }

    public @NotNull CategoryEnum getCategory() {
        return category;
    }

    public void setCategory(@NotNull CategoryEnum category) {
        this.category = category;
    }

    public @Positive @NotNull BigDecimal getPrice() {
        return price;
    }

    public void setPrice(@Positive @NotNull BigDecimal price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", category=" + category +
                ", price=" + price +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
