package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.mappers.MediaMapper;
import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.models.Platform;
import fr.backendt.cinephobia.models.dto.MediaDTO;
import fr.backendt.cinephobia.services.MediaService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.List;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

    @MockBean
    private MediaMapper mapper;

    private static List<Media> mediaList;
    private static List<MediaDTO> dtoList;

    @BeforeAll
    static void initTests() {
        Platform platform = new Platform(1L, "CinephobiaTV");

        mediaList = List.of(
                new Media(1L, "Ut et ligula condimentum", "https://lorem.com", List.of(platform)),
                new Media(2L, "Nam ipsum sapien, aliquet", "https://ipsum.com", List.of())
        );

        dtoList = List.of(
                new MediaDTO(1L, "Ut et ligula condimentum", "https://lorem.com", List.of(1L)),
                new MediaDTO(2L, "Nam ipsum sapien, aliquet", "https://ipsum.com", List.of())
        );
    }

    @BeforeEach
    void initMocks() {
        when(mapper.toDTO(any())).thenCallRealMethod();
        when(mapper.toEntity(any())).thenCallRealMethod();
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
    void getMediasWithoutParametersTest() throws Exception {
        // GIVEN
        RequestBuilder request = get("/media")
                .header("Hx-Request", "true");

        Page<Media> returnedPage = new PageImpl<>(mediaList);
        Sort defaultSort = Sort.by(Sort.Direction.DESC, "id");
        Pageable expectedPageable = PageRequest.of(0, 50, defaultSort);

        when(service.getMediaPage(any(), any()))
                .thenReturn(completedFuture(returnedPage));

        MvcResult result;
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments :: mediaList"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attributeExists("numberOfPages", "medias"))
                .andExpect(model().attribute("medias", dtoList));

        verify(service).getMediaPage(null, expectedPageable);
    }

    @Test
    void getMediasWithSearchTest() throws Exception {
        // GIVEN
        String search = "My media search";
        RequestBuilder request = get("/media")
                .header("Hx-Request", "true")
                .param("search", search);

        Page<Media> returnedPage = new PageImpl<>(mediaList);
        Sort defaultSort = Sort.by(Sort.Direction.DESC, "id");
        Pageable expectedPageable = PageRequest.of(0, 50, defaultSort);

        MvcResult result;

        when(service.getMediaPage(any(), any()))
                .thenReturn(completedFuture(returnedPage));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments :: mediaList"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attributeExists("numberOfPages", "medias"))
                .andExpect(model().attribute("medias", dtoList));

        verify(service).getMediaPage(search, expectedPageable);
    }

    @CsvSource({
            "2,2",
            "0,1",
            "-2,1"
    })
    @ParameterizedTest
    void getMediasWithSizeLimitTest(Integer sizeLimit, Integer expectedSizeLimit) throws Exception {
        // GIVEN
        RequestBuilder request = get("/media")
                .header("Hx-Request", "true")
                .param("size", String.valueOf(sizeLimit));

        Page<Media> returnedPage = new PageImpl<>(mediaList);
        Sort defaultSort = Sort.by(Sort.Direction.DESC, "id");
        Pageable expectedPageable = PageRequest.of(0, expectedSizeLimit, defaultSort);

        MvcResult result;

        when(service.getMediaPage(any(), any()))
                .thenReturn(completedFuture(returnedPage));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments :: mediaList"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attributeExists("numberOfPages", "medias"))
                .andExpect(model().attribute("medias", dtoList));

        verify(service).getMediaPage(null, expectedPageable);
    }

    @CsvSource({
            "id", "platformsId", "title"
    })
    @ParameterizedTest
    void getMediasWithSortingTest(String sortBy) throws Exception {
        // GIVEN
        RequestBuilder request = get("/media")
                .header("Hx-Request", "true")
                .param("sortBy", sortBy);

        Page<Media> returnedPage = new PageImpl<>(mediaList);
        Sort expectedSort = Sort.by(Sort.Direction.DESC, sortBy);
        Pageable expectedPageable = PageRequest.of(0, 50, expectedSort);

        MvcResult result;

        when(service.getMediaPage(any(), any()))
                .thenReturn(completedFuture(returnedPage));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments :: mediaList"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attributeExists("numberOfPages", "medias"))
                .andExpect(model().attribute("medias", dtoList));

        verify(service).getMediaPage(null, expectedPageable);
    }

    @CsvSource({
            "asc,asc", "desc,desc", "invalid,desc"
    })
    @ParameterizedTest
    void getMediasInOrderTest(String order, String expectedOrder) throws Exception {
        // GIVEN
        RequestBuilder request = get("/media")
                .header("Hx-Request", "true")
                .param("order", order);

        String defaultSort = "id";
        Page<Media> returnedPage = new PageImpl<>(mediaList);
        Pageable expectedPageable = PageRequest.of(0, 50, Sort.by(Sort.Direction.fromString(expectedOrder), defaultSort));

        MvcResult result;

        when(service.getMediaPage(any(), any()))
                .thenReturn(completedFuture(returnedPage));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments :: mediaList"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attributeExists("numberOfPages", "medias"))
                .andExpect(model().attribute("medias", dtoList));

        verify(service).getMediaPage(null, expectedPageable);
    }

    @CsvSource({
        "-1,0", "0,0", "2,2"
    })
    @ParameterizedTest
    void getMediaWithPageNumberTest(Integer pageIndex, Integer expectedIndex) throws Exception {
        // GIVEN
        RequestBuilder request = get("/media")
                .header("Hx-Request", "true")
                .param("page", String.valueOf(pageIndex));

        Page<Media> returnedPage = new PageImpl<>(mediaList);
        Sort expectedSort = Sort.by(Sort.Direction.DESC, "id");
        Pageable expectedPageable = PageRequest.of(expectedIndex, 50, expectedSort);

        MvcResult result;

        when(service.getMediaPage(any(), any()))
                .thenReturn(completedFuture(returnedPage));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments :: mediaList"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attributeExists("numberOfPages", "medias"))
                .andExpect(model().attribute("medias", dtoList));

        verify(service).getMediaPage(null, expectedPageable);
    }
}
