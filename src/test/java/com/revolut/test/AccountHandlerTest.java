package com.revolut.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.server.Account;
import com.revolut.server.ApplicationMain;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class AccountHandlerTest {

    private static ApplicationMain app;
    private static final String APP_URL = "http://localhost:9998";

    @BeforeClass
    public static void beforeClass() {
        app = new ApplicationMain();
        app.start();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        app.stop();
        System.exit(0);
    }

    @Test
    public void testPostAndGetAccount() throws Exception {
        String output = postAccountAmount("123", "200");
        Assert.assertEquals("Amount of account [123] was set to 200", output.trim());
        output = getAccount("1234");
        Assert.assertEquals("No such account [1234]", output.trim());
        Account acc = parseJson(getAccount("123"));
        Assert.assertEquals("123", acc.getAccount());
        Assert.assertEquals(200L, acc.getAmount().longValue());
    }

    @Test
    public void testTransferMoney() throws Exception {
        postAccountAmount("11", "200");
        postAccountAmount("12", "200");
        String output = transferMoney("11", "12", "-300");
        Assert.assertEquals("Transfer amount must be greater than 0", output.trim());

        output = transferMoney("11", "12", "300");
        Assert.assertEquals("Insufficient funds", output.trim());

        output = transferMoney("11", "12", "100");
        Assert.assertEquals("Amount 100 was transfered from [11] to [12]", output.trim());
        Account fromAcc = parseJson(getAccount("11"));
        Account toAcc = parseJson(getAccount("12"));
        Assert.assertEquals(100L, fromAcc.getAmount().longValue());
        Assert.assertEquals(300L, toAcc.getAmount().longValue());
    }


    public String getAccount(String account) {
        Client client = Client.create();
        WebResource webResource = client.resource(APP_URL + "/account/" + account);
        ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
        return response.getEntity(String.class);
    }

    public String postAccountAmount(String account, String amount) {
        Client client = Client.create();
        WebResource webResource = client.resource(APP_URL + "/account");
        String input = "{\"account\":\"" + account + "\",\"amount\":\"" + amount + "\"}";
        ClientResponse response = webResource.type("application/json").post(ClientResponse.class, input);
        return response.getEntity(String.class);
    }

    public String transferMoney(String from, String to, String amount) {
        Client client = Client.create();
        WebResource webResource = client.resource(APP_URL + "/account/transfer");
        String input = "{\"from\":\"" + from + "\", \"to\":\"" + to + "\", \"amount\":\"" + amount + "\"}";
        ClientResponse response = webResource.type("application/json").post(ClientResponse.class, input);
        return response.getEntity(String.class);
    }

    public Account parseJson(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, Account.class);
    }
}
