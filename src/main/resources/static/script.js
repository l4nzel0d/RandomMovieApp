async function fetchRandomMovie() {
    try {
        const response = await fetch('/random');
        if (!response.ok) throw new Error('Failed to fetch movie');
        const movie = await response.json();
        displayMovie(movie);
    } catch (error) {
        console.error(error);
        document.getElementById('movie').innerHTML = '<p>Error loading movie.</p>';
    }
}

function displayMovie(movie) {
    document.getElementById('movie-title').textContent = `${movie.title} (${movie.year})`;

    const poster = document.getElementById('movie-poster');
    poster.src = movie.posterUrl;
    poster.alt = `Poster of ${movie.title}`;

    document.getElementById('movie-genre').textContent = movie.genre;
    document.getElementById('movie-actors').textContent = movie.actors.join(', ');
}


document.getElementById('new-movie').addEventListener('click', fetchRandomMovie);

// Fetch movie on initial page load
fetchRandomMovie();
