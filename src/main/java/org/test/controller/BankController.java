package org.test.controller;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.test.constant.UrlConstant;
import org.test.model.dto.BankDto;
import org.test.model.entity.Bank;
import org.test.service.BankService;

import java.util.List;

@Path(UrlConstant.BANK)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BankController {

    @Inject
    BankService bankService;

    @POST
    @Path(UrlConstant.CREATE_BANK)
    public Response saveBank(@Valid BankDto bankDto) {
        String response = bankService.addBank(bankDto);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path(UrlConstant.GET_BANK + "/{id}")
    public Response getBankByName(@PathParam("id") Long content) {
        Bank bankList = bankService.getBankByName(content);
        return Response.ok(bankList).build();
    }

    @PUT
    @Path(UrlConstant.BANK_UPDATE + "/{bankId}")
    public Response updateBank(@Valid BankDto bankDto, @PathParam("bankId") Long bankId) {
        String response = bankService.updateBank(bankDto, bankId);
        return Response.ok(response).build();
    }

    @DELETE
    @Path(UrlConstant.BANK_DELETE + "/{bankId}")
    public Response deleteBank(@PathParam("bankId") Long bankId) {
        Response response = bankService.deleteBank(bankId);
        return Response.ok(response).build();
    }

    @GET
    @Path(UrlConstant.GET_ALL_BANK)
    public Response getAllBank() {
        List<Bank> bankList = bankService.getAllBank();
        return Response.ok(bankList).build();
    }
}
