package com.microservico.product.exceptions;

import java.io.Serial;

public class RecordNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public RecordNotFoundException(Long id, String resourceName) {
        super(String.format("%s not found with id: %d", resourceName, id));
    }

    public RecordNotFoundException(Long id) {
        super("Record not found with id: " + id);
    }
}
