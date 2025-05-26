package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class MovieServer {

    public static void main(String[] args) throws IOException {
        int port = 8080; // The port our server will listen on
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Create a simple context handler for the root path "/"
        server.createContext("/", new StaticFileHandler("index.html"));
        server.createContext("/styles.css", new StaticFileHandler("styles.css"));
        server.createContext("/script.js", new StaticFileHandler("script.js"));

        server.setExecutor(null); // Use the default executor
        server.start();
        System.out.println("Server started successfully on port " + port);
        System.out.println("Visit http://localhost:" + port + " in your browser.");
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

    static class StaticFileHandler implements HttpHandler {
        private final String fileName;
        private static final Map<String, String> MIME_TYPES = new HashMap<>();
        static {
            MIME_TYPES.put("html", "text/html; charset=UTF-8");
            MIME_TYPES.put("css", "text/css; charset=UTF-8");
            MIME_TYPES.put("js", "application/javascript; charset=UTF-8");
            MIME_TYPES.put("json", "application/json; charset=UTF-8");
            MIME_TYPES.put("png", "image/png");
            MIME_TYPES.put("jpg", "image/jpeg");
            MIME_TYPES.put("jpeg", "image/jpeg");
            // add more if needed
        }

        public StaticFileHandler(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String extension = getExtension(fileName);
            String contentType = MIME_TYPES.getOrDefault(extension, "application/octet-stream");

            InputStream is = getClass().getClassLoader().getResourceAsStream("static/" + fileName);
            if (is == null) {
                String response = "404 (Not Found)\n";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            byte[] bytes = is.readAllBytes();
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
            is.close();
        }

        private String getExtension(String fileName) {
            int lastDot = fileName.lastIndexOf('.');
            return lastDot == -1 ? "" : fileName.substring(lastDot + 1);
        }
    }
}

