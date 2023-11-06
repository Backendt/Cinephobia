package fr.backendt.cinephobia.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class TheMovieDBConfig {

    public static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w400/";
    public static final String HD_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/original/";
    private static final String V3_API_URL = "https://api.themoviedb.org/3";

    @Bean
    public WebClient tmdbAPI() {
        return WebClient.create(V3_API_URL);
    }

}
