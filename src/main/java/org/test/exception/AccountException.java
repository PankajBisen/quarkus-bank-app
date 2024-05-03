package org.test.exception;

import jakarta.ws.rs.core.Response;
import lombok.Getter;

@Getter
public class AccountException extends RuntimeException {
    private final Response.Status status;

    public AccountException(String message, Response.Status status) {
        super(message);
        this.status = status;
    }

    public Response toResponse() {
        return Response.status(status).entity(getMessage()).build();
    }
}
