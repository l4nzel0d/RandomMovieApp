package org.example;

import java.util.List;

public record Movie(
        String title,
        String posterUrl,
        int year,
        String genre,
        List<String> actors
) { }
