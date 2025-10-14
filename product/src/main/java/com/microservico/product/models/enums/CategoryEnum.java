package com.microservico.product.models.enums;

public enum CategoryEnum {
    PERIPHERALS("Perifericos"),
    INTERNAL_COMPONENTS("Componentes Internos"),
    COMPUTERS("Computadores"),
    SOFTWARE("Software"),
    ACCESSORIES("Acessórios");

    private final String displayName;

    CategoryEnum(String displayName) {
        this.displayName = displayName;
    }

}
