package com.revolut.server;

import com.sun.net.httpserver.HttpServer;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;


public class ApplicationMain {

    public final static int port = 9998;
    public final static String host="http://localhost/";
    private HttpServer server;

    public void start(){
        System.out.println("Server started");
        URI baseUri = UriBuilder.fromUri(host).port(port).build();
        ResourceConfig config = new ResourceConfig(AccountHandler.class);
        server = JdkHttpServerFactory.createHttpServer(baseUri, config);
    }

    public void stop(){
        server.stop(0);
    }

    public static void main(String[] args) {
        ApplicationMain app = new ApplicationMain();
        app.start();
    }
}
