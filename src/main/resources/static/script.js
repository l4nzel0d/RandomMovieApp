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
    const movieDiv = document.getElementById('movie');
    movieDiv.innerHTML = `
        <h2>${movie.title} (${movie.year})</h2>
        <img src="${movie.posterUrl}" alt="Poster of ${movie.title}" />
        <p><strong>Genre:</strong> ${movie.genre}</p>
        <p><strong>Actors:</strong> ${movie.actors.join(', ')}</p>
    `;
}

document.getElementById('new-movie').addEventListener('click', fetchRandomMovie);

// Fetch movie on initial page load
fetchRandomMovie();
