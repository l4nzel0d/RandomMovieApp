package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class MovieServer {

    public static void main(String[] args) throws IOException {
        int port = 8080; // The port our server will listen on
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Create a simple context handler for the root path "/"
        server.createContext("/", new MySimpleHandler());

        server.setExecutor(null); // Use the default executor
        server.start();
        System.out.println("Server started successfully on port " + port);
        System.out.println("Visit http://localhost:" + port + " in your browser.");
        System.in.read();
    }

    // A very basic HTTP Handler that always returns "Hello from Movie Server!"
    static class MySimpleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Hello from Movie Server!";
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
