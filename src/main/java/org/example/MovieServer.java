package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MovieServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Load movies.json from resources
        InputStream moviesStream = MovieServer.class.getClassLoader().getResourceAsStream("movies.json");
        if (moviesStream == null) {
            System.err.println("movies.json not found!");
            System.exit(1);
        }
        String json = new String(moviesStream.readAllBytes(), StandardCharsets.UTF_8);
        Type movieListType = new TypeToken<List<Movie>>(){}.getType();
        List<Movie> movies = new Gson().fromJson(json, movieListType);

        // Static file handlers
        server.createContext("/", new StaticFileHandler("index.html"));
        server.createContext("/styles.css", new StaticFileHandler("styles.css"));
        server.createContext("/script.js", new StaticFileHandler("script.js"));

        // Random movie API handler
        server.createContext("/random", new RandomMovieHandler(movies));

        server.setExecutor(null);
        server.start();

        System.out.println("Server started successfully on port " + port);
        System.out.println("Visit http://localhost:" + port + " in your browser.");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Stopping server...");
            server.stop(0);
        }));

        Thread.currentThread().join();
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

    static class RandomMovieHandler implements HttpHandler {
        private final List<Movie> movies;
        private final Random random = new Random();
        private final Gson gson = new Gson();
        private int lastIndex = -1;  // Store last sent movie index

        public RandomMovieHandler(List<Movie> movies) {
            this.movies = movies;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            int newIndex;

            if (movies.size() == 1) {
                newIndex = 0; // Only one movie available, no choice
            } else {
                do {
                    newIndex = random.nextInt(movies.size());
                } while (newIndex == lastIndex);
            }

            lastIndex = newIndex;
            Movie randomMovie = movies.get(newIndex);
            String jsonResponse = gson.toJson(randomMovie);

            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

}

