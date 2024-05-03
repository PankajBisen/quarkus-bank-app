package org.test.controller;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.test.constant.UrlConstant;
import org.test.model.dto.MoneyTransferDto;
import org.test.model.entity.Transaction;
import org.test.service.TransactionService;

import java.util.List;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionController {

    @Inject
    TransactionService transactionService;

    @PUT
    @Path(UrlConstant.TRANSFER_MONEY)
    public Response transferMoney(@Valid MoneyTransferDto transactionDto) {
        String response = transactionService.transferMoney(transactionDto);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path(UrlConstant.ACCOUNT_TRANSACTION_BY_ID + "/{accountNumberFrom}")
    public Response transaction(@PathParam("accountNumberFrom") String accountNumberFrom) {
        List<Transaction> transactionList = transactionService.transaction(accountNumberFrom);
        return Response.ok(transactionList).build();
    }

    @GET
    @Path(UrlConstant.TRANSACTION_BY_DAYS + "/{numberOfDays}")
    public Response transactionByDays(@PathParam("numberOfDays") Long numberOfDays) {
        List<Transaction> transactionList = transactionService.transactionByDays(numberOfDays);
        return Response.ok(transactionList).build();
    }
}
