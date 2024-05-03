package org.test.controller;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.test.constant.UrlConstant;
import org.test.model.dto.CustomerDto;
import org.test.model.dto.CustomerUpdateDto;
import org.test.service.CustomerService;

import java.util.List;

@Path(UrlConstant.CUSTOMER_URL)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerController {

    @Inject
    CustomerService customerService;

    @POST
    @Path(UrlConstant.CUSTOMER_CREATE)
    public Response save(@Valid CustomerDto customerDto) {
        String response = customerService.save(customerDto);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path(UrlConstant.GET_BY_NAME_OR_MOBILENO_OR_EMAILID + "/{content}")
    public Response getByEmailOrName(@PathParam("content") String content) {
        List<CustomerDto> customerList = customerService.getByEmailOrName(content);
        return Response.ok(customerList).build();
    }

    @GET
    @Path(UrlConstant.GET_ALL)
    public Response getAllCustomer() {
        List<CustomerDto> customerList = customerService.getAllCustomer();
        return Response.ok(customerList).build();
    }

    @PUT
    @Path(UrlConstant.UPDATE_CUSTOMER + "/{customerId}")
    public Response updateCustomer(@Valid CustomerUpdateDto customerDto, @PathParam("customerId") Long customerId) {
        String response = customerService.updateCustomer(customerDto, customerId);
        return Response.ok(response).build();
    }

    @DELETE
    @Path(UrlConstant.DELETE_CUSTOMER + "/{customerId}")
    public Response deleteCustomer(@PathParam("customerId") Long customerId) {
        String response = customerService.deleteCustomer(customerId);
        return Response.ok(response).build();
    }

    @GET
    @Path(UrlConstant.GET_ALL_CUSTOMERS_WITHOUT_ACCOUNTS + "/{bankId}")
    public Response getAllCustomerByBankId(@PathParam("bankId") Long bankId) {
        List<CustomerDto> customerList = customerService.getAllCustomerByBankId(bankId);
        return Response.ok(customerList).build();
    }

    @GET
    @Path(UrlConstant.GET_ALL_CUSTOMER_BY_BANK_ID + "/{bankId}")
    public Response getAllByBankId(@PathParam("bankId") Long bankId) {
        List<CustomerDto> customerList = customerService.getAllByBankId(bankId);
        return Response.ok(customerList).build();
    }
}
