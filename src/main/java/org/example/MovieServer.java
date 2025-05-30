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
import java.util.*;

/**
 * A simple HTTP server that serves static files and provides a REST API endpoint
 * to return a random movie in JSON format from a preloaded list.
 */
public class MovieServer {

    /**
     * Entry point to start the HTTP server.
     *
     * @param args command line arguments (not used)
     * @throws IOException          if I/O error occurs during server startup or resource loading
     * @throws InterruptedException if the main thread is interrupted while waiting
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        InputStream moviesStream = MovieServer.class.getClassLoader().getResourceAsStream("movies.json");
        if (moviesStream == null) {
            System.err.println("movies.json not found!");
            System.exit(1);
        }
        String json = new String(moviesStream.readAllBytes(), StandardCharsets.UTF_8);
        Type movieListType = new TypeToken<List<Movie>>(){}.getType();
        List<Movie> movies = new Gson().fromJson(json, movieListType);

        server.createContext("/", new StaticFileHandler("index.html"));
        server.createContext("/styles.css", new StaticFileHandler("styles.css"));
        server.createContext("/script.js", new StaticFileHandler("script.js"));

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

    /**
     * HTTP handler for serving static files from resources under 'static/' directory.
     */
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
        }

        /**
         * Creates a handler that serves the specified static file.
         *
         * @param fileName the file name to serve, relative to 'static/' directory in resources
         */
        public StaticFileHandler(String fileName) {
            this.fileName = fileName;
        }

        /**
         * Handles the HTTP request by returning the file content or a 404 if not found.
         *
         * @param exchange the HTTP exchange containing request and response objects
         * @throws IOException if an I/O error occurs
         */
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String extension = getExtension(fileName);
            String contentType = MIME_TYPES.getOrDefault(extension, "application/octet-stream");

            InputStream is = getClass().getClassLoader().getResourceAsStream("static/" + fileName);
            if (is == null) {
                String response = "404 (Not Found)\n";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            byte[] bytes = is.readAllBytes();
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
            is.close();
        }

        /**
         * Extracts the file extension from a file name.
         *
         * @param fileName the file name
         * @return the file extension without the dot, or empty string if none
         */
        private String getExtension(String fileName) {
            int lastDot = fileName.lastIndexOf('.');
            return lastDot == -1 ? "" : fileName.substring(lastDot + 1);
        }
    }

    /**
     * HTTP handler for serving a random movie in JSON format.
     * Ensures the same movie is not returned consecutively.
     */
    static class RandomMovieHandler implements HttpHandler {
        private final List<Movie> originalMovies;
        private final List<Movie> movieQueue = new ArrayList<>();
        private final Random random = new Random();
        private final Gson gson = new Gson();
        private int currentIndex = 0;

        /**
         * Constructs a handler with a list of movies to serve in a shuffled, non-repeating cycle.
         *
         * @param movies the list of movies available to serve
         */
        public RandomMovieHandler(List<Movie> movies) {
            this.originalMovies = new ArrayList<>(movies);
            reshuffleQueue();
        }

        /**
         * Reshuffles the movie queue randomly and resets the current index.
         */
        private void reshuffleQueue() {
            movieQueue.clear();
            movieQueue.addAll(originalMovies);
            Collections.shuffle(movieQueue, random);
            currentIndex = 0;
        }

        /**
         * Handles the HTTP request by responding with a JSON of the next movie in a shuffled sequence.
         *
         * @param exchange the HTTP exchange containing request and response
         * @throws IOException if an I/O error occurs
         */
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (currentIndex >= movieQueue.size()) {
                reshuffleQueue();
            }

            Movie movie = movieQueue.get(currentIndex++);
            String jsonResponse = gson.toJson(movie);

            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

}
