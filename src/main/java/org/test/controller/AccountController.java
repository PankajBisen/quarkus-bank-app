package org.test.controller;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.test.constant.UrlConstant;
import org.test.model.dto.AccountDto;
import org.test.service.AccountService;
import java.util.List;

@Path(UrlConstant.ACCOUNT)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountController {
    @Inject
    AccountService accountService;

    @POST
    @Path(UrlConstant.ACCOUNT_CREAT)
    public Response save(@Valid AccountDto accountDto) {
        String response = accountService.saveAccountNo(accountDto);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path(UrlConstant.GET_ACCOUNT)
    public Response accountByAccNo(@PathParam("content") String content) {
        List<AccountDto> accountDtoList = accountService.accountByAccNo(content);
        return Response.ok(accountDtoList).build();
    }

    @GET
    @Path(UrlConstant.ALL_BANK_ACCOUNT)
    public Response getAllAccount() {
        List<AccountDto> accountDtoList = accountService.getAllAccount();
        return Response.ok(accountDtoList).build();
    }

    @PUT
    @Path(UrlConstant.UPDATE_ACCOUNT + "/{accountId}")
    public Response updateBank(@Valid AccountDto accountDto, @PathParam("accountId") Long accountId) {
        String response = accountService.updateAccount(accountDto, accountId);
        return Response.ok(response).build();
    }

    @DELETE
    @Path(UrlConstant.DELETE_ACCOUNT + "/{accountId}")
    public Response deleteAccount(@PathParam("accountId") Long accountId) {
        String response = accountService.deleteAccount(accountId);
        return Response.ok(response).build();
    }

    @GET
    @Path(UrlConstant.GET_ALL_BANK_BY_ID + "/{bankId}")
    public Response getAllByBankId(@PathParam("bankId") Long bankId) {
        List<AccountDto> accountDtoList = accountService.getAllByBankId(bankId);
        return Response.ok(accountDtoList).build();
    }
}
