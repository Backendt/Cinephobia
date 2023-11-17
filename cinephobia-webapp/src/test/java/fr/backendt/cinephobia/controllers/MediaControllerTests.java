package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.exceptions.EntityNotFoundException;
import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.models.tmdb.SearchResults;
import fr.backendt.cinephobia.services.MediaService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.List;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser
@WebMvcTest(MediaController.class)
class MediaControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MediaService service;

    private static List<Media> mediaList;

    @BeforeAll
    static void initTests() {
        mediaList = List.of(
                new Media(1L, fr.backendt.cinephobia.models.MediaType.TV, "Ut et ligula condimentum", "Description one", "/lorem"),
                new Media(2L, fr.backendt.cinephobia.models.MediaType.MOVIE, "Nam ipsum sapien, aliquet", "Description two", "/ipsum")
        );
    }

    @Test
    void getMediaPageTest() throws Exception {
        // GIVEN
        RequestBuilder request = get("/media");

        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isOk())
                .andExpect(view().name("medias"))
                .andExpect(model().hasNoErrors());
    }

    @Test
    void getMediasTest() throws Exception {
        // GIVEN
        RequestBuilder request = get("/media")
                .header("Hx-Request", "true");

        int defaultPage = 1;
        SearchResults searchResults = new SearchResults(defaultPage, 1, mediaList.size(), mediaList);

        when(service.getMedias(any(), anyInt())).thenReturn(completedFuture(searchResults));
        MvcResult result;
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/medias :: mediaList"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("mediasPage", searchResults));

        verify(service).getMedias(null, defaultPage);
    }

    @Test
    void getMediasWithSearchTest() throws Exception {
        // GIVEN
        String search = "java";

        RequestBuilder request = get("/media")
                .header("Hx-Request", "true")
                .param("search", search);

        int defaultPage = 1;
        SearchResults searchResults = new SearchResults(defaultPage, 1, mediaList.size(), mediaList);

        when(service.getMedias(any(), anyInt())).thenReturn(completedFuture(searchResults));
        MvcResult result;
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/medias :: mediaList"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("mediasPage", searchResults));

        verify(service).getMedias(search, defaultPage);
    }

    @Test
    void getMediasWithPageTest() throws Exception {
        // GIVEN
        int page = 4;
        RequestBuilder request = get("/media")
                .header("Hx-Request", "true")
                .param("page", String.valueOf(page));

        SearchResults searchResults = new SearchResults(page, page, mediaList.size(), mediaList);

        when(service.getMedias(any(), anyInt())).thenReturn(completedFuture(searchResults));
        MvcResult result;
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/medias :: mediaList"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("mediasPage", searchResults));

        verify(service).getMedias(null, page);
    }

    @Test
    void getMovieTest() throws Exception {
        // GIVEN
        long movieId = 1L;
        RequestBuilder request = get("/media/movie/" + movieId);

        Media media = mediaList.get(0);
        MvcResult result;

        when(service.getMovie(any())).thenReturn(completedFuture(media));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("media", media))
                .andExpect(view().name("media"));

        verify(service, never()).getSeries(any());
        verify(service).getMovie(movieId);
    }

    @Test
    void getSeriesTest() throws Exception {
        // GIVEN
        long seriesId = 1L;
        RequestBuilder request = get("/media/tv/" + seriesId);

        Media media = mediaList.get(0);
        MvcResult result;

        when(service.getSeries(any())).thenReturn(completedFuture(media));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("media", media))
                .andExpect(view().name("media"));

        verify(service).getSeries(seriesId);
        verify(service, never()).getMovie(any());
    }

    @Test
    void getSeriesFromUnknownMediaTypeTest() throws Exception {
        // GIVEN
        long seriesId = 1L;
        RequestBuilder request = get("/media/test/" + seriesId);

        Media media = mediaList.get(0);
        MvcResult result;

        when(service.getSeries(any())).thenReturn(completedFuture(media));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("media", media))
                .andExpect(view().name("media"));

        verify(service).getSeries(seriesId);
        verify(service, never()).getMovie(any());
    }

    @Test
    void getUnknownMediaTest() throws Exception {
        // GIVEN
        long movieId = 1L;
        RequestBuilder request = get("/media/movie/" + movieId);

        MvcResult result;

        when(service.getMovie(any()))
                .thenReturn(failedFuture(new EntityNotFoundException("Movie not found")));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());

        verify(service).getMovie(movieId);
    }

}
