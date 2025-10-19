package com.microservico.sales.models;

import jakarta.persistence.*;

import java.io.Serial;

@Entity
@Table(name = "sales")
public class Sale {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
