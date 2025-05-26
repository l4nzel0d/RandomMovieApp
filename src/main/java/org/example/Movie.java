package org.example;

import java.util.List;

/**
 * Represents a movie with its basic information.
 *
 * @param title the title of the movie
 * @param posterUrl the URL to the movie's poster image
 * @param year the release year of the movie
 * @param genre the genre of the movie
 * @param actors the list of main actors in the movie
 */
public record Movie(
        String title,
        String posterUrl,
        int year,
        String genre,
        List<String> actors
) { }
