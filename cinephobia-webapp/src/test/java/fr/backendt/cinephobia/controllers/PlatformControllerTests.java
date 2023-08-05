package fr.backendt.cinephobia.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.backendt.cinephobia.models.Platform;
import fr.backendt.cinephobia.services.PlatformService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlatformController.class)
class PlatformControllerTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlatformService service;

    private Platform platformTest;

    @BeforeEach
    void initTestPlatform() {
        platformTest = new Platform("New Platform");
    }

    @Test
    void createPlatformTest() throws Exception {
        // GIVEN
        String platformData = objectMapper.writeValueAsString(platformTest);
        String requestUrl = "/api/v1/platform";
        RequestBuilder request = post(requestUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(platformData);

        when(service.createPlatform(any()))
                .thenReturn(CompletableFuture.completedFuture(platformTest));

        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(platformTest));

        verify(service).createPlatform(platformTest);
    }

    @Test
    void getAllPlatformsTest() throws Exception {
        // GIVEN
        String requestUrl = "/api/v1/platform";
        RequestBuilder request = get(requestUrl);

        List<Platform> platforms = List.of(platformTest);

        when(service.getAllPlatforms())
                .thenReturn(CompletableFuture.completedFuture(platforms));

        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(platforms));
    }

    @Test
    void searchPlatformsTest() throws Exception {
        // GIVEN
        String searchString = "new";
        String requestUrl = "/api/v1/platform";
        RequestBuilder request = get(requestUrl)
                .param("search", searchString);

        List<Platform> platforms = List.of(platformTest);

        when(service.getPlatformsContainingInName(any()))
                .thenReturn(CompletableFuture.completedFuture(platforms));
        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(platforms));

        verify(service).getPlatformsContainingInName(searchString);
    }

    @Test
    void getPlatformTest() throws Exception {
        // GIVEN
        Long platformId = 1L;
        String requestUrl = "/api/v1/platform/{id}";
        RequestBuilder request = get(requestUrl, platformId);

        when(service.getPlatform(any()))
                .thenReturn(CompletableFuture.completedFuture(platformTest));
        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(platformTest));

        verify(service).getPlatform(platformId);
    }

}
