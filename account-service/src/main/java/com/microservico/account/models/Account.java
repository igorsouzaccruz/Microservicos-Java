package com.microservico.account.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "accounts")
public class Account implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @NotNull
    @Length(max = 100)
    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @NotBlank
    @NotNull
    @Length(max = 255)
    @Column(length = 255, nullable = false)
    private String password;

    @NotBlank
    @NotNull
    @Length(max = 300)
    @Column(length = 300, nullable = false)
    private String address;

    public Account() {
    }

    public Account(String email, String password, String address) {
        this.email = email;
        this.password = password;
        this.address = address;
    }

    public Account(Long id, String email, String password, String address) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotBlank @NotNull @Length(max = 100) String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank @NotNull @Length(max = 100) String email) {
        this.email = email;
    }

    public @NotBlank @NotNull @Length(max = 255) String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank @NotNull @Length(max = 255) String password) {
        this.password = password;
    }

    public @NotBlank @NotNull @Length(max = 300) String getAddress() {
        return address;
    }

    public void setAddress(@NotBlank @NotNull @Length(max = 300) String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
