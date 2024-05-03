package org.test.exception;

import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.test.exception.AccountException;
import org.test.exception.BankException;
import org.test.exception.CustomerException;
import org.test.exception.TransactionException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton // Use @Singleton instead of @RestControllerAdvice
@Provider // Use @Provider for ExceptionMapper
public class CustomGlobalException implements ExceptionMapper<Throwable> {

    private static Map<String, Object> prepareExceptionResponse(Response.Status status, Object message) {
        Map<String, Object> exceptionBody = new HashMap<>();
        exceptionBody.put("timestamp", new Date());
        exceptionBody.put("status", status.getStatusCode());
        exceptionBody.put("message", message);
        return exceptionBody;
    }

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof ConstraintViolationException) {
            List<String> messages = ((ConstraintViolationException) exception).getConstraintViolations().stream().map(constraintViolation -> constraintViolation.getMessage()).collect(Collectors.toList());
            return Response.status(Response.Status.BAD_REQUEST).entity(prepareExceptionResponse(Response.Status.BAD_REQUEST, messages)).build();
        } else if (exception instanceof BankException bankException) {
            return Response.status(bankException.getStatus()).entity(prepareExceptionResponse(bankException.getStatus(), bankException.getMessage())).build();
        } else if (exception instanceof CustomerException customerException) {
            return Response.status(customerException.getStatus()).entity(prepareExceptionResponse(customerException.getStatus(), customerException.getMessage())).build();
        } else if (exception instanceof AccountException accountException) {
            return Response.status(accountException.getStatus()).entity(prepareExceptionResponse(accountException.getStatus(), accountException.getMessage())).build();
        } else if (exception instanceof TransactionException transactionException) {
            return Response.status(transactionException.getStatus()).entity(prepareExceptionResponse(transactionException.getStatus(), transactionException.getMessage())).build();
        }

        // Handle other exceptions (optional)
        return Response.serverError().entity(prepareExceptionResponse(Response.Status.INTERNAL_SERVER_ERROR, "Internal server error")).build();
    }
}
