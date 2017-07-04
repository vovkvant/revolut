package com.revolut.server;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Path("/account")
public class AccountHandler {
    private static ConcurrentMap<String, Account> accountsMap = new ConcurrentHashMap<String, Account>();

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAccountAmount(@PathParam("id") String id) {
        Account account = accountsMap.get(id);
        if (account == null) {
            return Response.status(500).entity("No such account [" + id + "]").build();
        } else return Response.status(201).entity(account).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setAccountAmount(Account account) {
        accountsMap.put(account.getAccount(), account);
        String result = "Amount of account [" + account.getAccount() + "] was set to " + account.getAmount().toString();
        return Response.status(201).entity(result).build();
    }

    @POST
    @Path("/transfer")
    public Response transfer(TransferRequest transferRequest) {
        if (transferRequest.getAmount() <= 0) {
            return Response.status(500).entity("Transfer amount must be greater than 0").build();
        }
        Account fromAcc = accountsMap.get(transferRequest.getFrom());
        Account toAcc = accountsMap.get(transferRequest.getTo());
        if (fromAcc == null || toAcc == null) {
            return Response.status(500).entity("Incorrect accounts were specified").build();
        }

        Account lock1;
        Account lock2;
        //to avoid deadlock
        if (fromAcc.account.compareTo(toAcc.account) > 0) {
            lock1 = fromAcc;
            lock2 = toAcc;
        } else {
            lock1 = toAcc;
            lock2 = fromAcc;
        }
        synchronized (lock1) {
            synchronized (lock2) {
                if (transferRequest.getAmount() > fromAcc.amount) {
                    return Response.status(500).entity("Insufficient funds").build();
                }
                fromAcc.amount -= transferRequest.getAmount();
                toAcc.amount += transferRequest.getAmount();
                String result = "Amount " + transferRequest.getAmount() + " was transfered from ["
                        + fromAcc.getAccount() + "] to [" + toAcc.getAccount() + "]";
                return Response.status(201).entity(result).build();
            }
        }
    }
}
