# Cinéphobia
[![GPLv3 License](https://img.shields.io/badge/License-GPL%20v3-yellow.svg)](https://opensource.org/licenses/)

Cinéphobia is a web application that helps users identify media containing specific phobia triggers. By leveraging community contributions, it offers detailed warnings about phobias in movies, TV shows, and more.

Cinéphobia makes media consumption safer and more enjoyable by helping people avoid unwanted triggers.

## Screenshots

![Medias page preview](https://raw.githubusercontent.com/Backendt/Cinephobia/main/images/media_cinephobia.png)
![Media page preview](https://raw.githubusercontent.com/Backendt/Cinephobia/main/images/movie_cinephobia.png)
![Phone medias page preview](https://raw.githubusercontent.com/Backendt/Cinephobia/main/images/phone_preview.png)

## Installation

First, clone the project to your machine
```bash
git clone https://github.com/Backendt/Cinephobia.git
```
Then, go to the project directory and fill the `.env` file.

Make sure to set a valid TMDB API key to `TMDB_JWT=`.
You can follow the instructions [here](https://developer.themoviedb.org/docs/getting-started).

Finally, you can run the docker containers with:
```bash
docker compose up
```
Cinéphobia will be up on port 8080 !

## Credits
- [TMDB](https://www.themoviedb.org/)
