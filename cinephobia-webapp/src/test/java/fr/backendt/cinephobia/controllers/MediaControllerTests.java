package fr.backendt.cinephobia.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.backendt.cinephobia.controllers.api.v1.MediaController;
import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.mappers.MediaMapper;
import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.models.Platform;
import fr.backendt.cinephobia.models.dto.MediaDTO;
import fr.backendt.cinephobia.services.MediaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser
@WebMvcTest(MediaController.class)
class MediaControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MediaService service;

    @MockBean
    private MediaMapper mapper;

    private Media mediaTest;
    private MediaDTO mediaDTOTest;

    @BeforeEach
    void initTests() {
        Platform platform = new Platform();
        platform.setId(1L);

        mediaTest = new Media("1 function, 1000 tests", "https://example.com/help.png", List.of(platform));
        mediaDTOTest = new MediaDTO(null, "1 function, 1000 tests", "https://example.com/help.png", List.of(1L));

        when(mapper.toDTO(mediaTest)).thenReturn(mediaDTOTest);
        when(mapper.toEntity(mediaDTOTest)).thenReturn(mediaTest);
        when(mapper.toDTOs(List.of(mediaTest))).thenReturn(List.of(mediaDTOTest));
    }

    @Test
    void createMediaTest() throws Exception {
        // GIVEN
        String mediaData = new ObjectMapper().writeValueAsString(mediaDTOTest);

        String requestUrl = "/api/v1/media";
        RequestBuilder request = post(requestUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mediaData)
                .with(csrf());

        when(service.createMedia(any()))
                .thenReturn(CompletableFuture.completedFuture(mediaTest));
        // WHEN
        mvc.perform(request)
                // THEN
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(mediaDTOTest));

        verify(service).createMedia(mediaTest);
    }

    @ParameterizedTest
    @CsvSource(
            value = {
                    "    ,https://example.com/",
                    "a,https://example.com/",
                    "Title,javascript:alert(1)",
                    "Title,http://example.com/",
                    "Title,   ",
                    "Title,"
            }, ignoreLeadingAndTrailingWhitespace = false)
    void createInvalidMediaTest(String title, String imageUrl) throws Exception {
        // GIVEN
        MediaDTO invalidMedia = new MediaDTO(null, title, imageUrl, List.of(1L));
        String mediaData = new ObjectMapper().writeValueAsString(invalidMedia);

        String requestUrl = "/api/v1/media";
        RequestBuilder request = post(requestUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mediaData)
                .with(csrf());
        // WHEN
        mvc.perform(request)
                // THEN
                .andExpect(status().isBadRequest());

        verify(service, never()).createMedia(any());
    }

    @Test
    void createDuplicateMediaTest() throws Exception {
        // GIVEN
        String mediaData = new ObjectMapper().writeValueAsString(mediaDTOTest);

        String requestUrl = "/api/v1/media";
        RequestBuilder request = post(requestUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mediaData)
                .with(csrf());

        when(service.createMedia(any())).thenThrow(EntityException.class);
        // WHEN
        mvc.perform(request)
                // THEN
                .andExpect(status().isBadRequest());

        verify(service).createMedia(mediaTest);
    }

    @Test
    void getAllMediasTest() throws Exception {
        // GIVEN
        String requestUrl = "/api/v1/media";
        RequestBuilder request = get(requestUrl);

        List<Media> medias = List.of(mediaTest);
        List<MediaDTO> expected = List.of(mediaDTOTest);
        when(service.getAllMedias())
                .thenReturn(CompletableFuture.completedFuture(medias));
        // WHEN
        mvc.perform(request)
                // THEN
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(expected));

        verify(service).getAllMedias();
    }

    @Test
    void getMediasContainingStringTest() throws Exception {
        // GIVEN
        String mediaSearch = "test";
        String requestUrl = "/api/v1/media";
        RequestBuilder request = get(requestUrl)
                .param("search", mediaSearch);

        List<Media> medias = List.of(mediaTest);
        List<MediaDTO> expected = List.of(mediaDTOTest);

        when(service.getMediaContainingInTitle(any()))
                .thenReturn(CompletableFuture.completedFuture(medias));
        // WHEN
        mvc.perform(request)
                // THEN
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(expected));


        verify(service).getMediaContainingInTitle(mediaSearch);
    }

    @Test
    void getMediaByIdTest() throws Exception {
        // GIVEN
        Long mediaId = 1L;
        String requestUrl = "/api/v1/media/{id}";
        RequestBuilder request = get(requestUrl, mediaId);

        when(service.getMedia(any()))
                .thenReturn(CompletableFuture.completedFuture(mediaTest));
        // WHEN
        mvc.perform(request)
                // THEN
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(mediaDTOTest));

        verify(service).getMedia(mediaId);
    }

    @Test
    void getUnknownMediaByIdTest() throws Exception {
        // GIVEN
        Long mediaId = 1L;
        String requestUrl = "/api/v1/media/{id}";
        RequestBuilder request = get(requestUrl, mediaId);

        when(service.getMedia(any()))
                .thenThrow(EntityException.EntityNotFoundException.class);
        // WHEN
        mvc.perform(request)
                // THEN
                .andExpect(status().isNotFound());

        verify(service).getMedia(mediaId);
    }

}
