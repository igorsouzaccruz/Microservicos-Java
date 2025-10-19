package com.microservico.sales.exceptions;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

public record ExceptionResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

}
