package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.models.*;
import fr.backendt.cinephobia.models.dto.TriggerDTO;
import fr.backendt.cinephobia.models.dto.WarnResponseDTO;
import fr.backendt.cinephobia.services.TriggerService;
import fr.backendt.cinephobia.services.WarnService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser
@WebMvcTest(WarnController.class)
class WarnControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private WarnService service;

    @MockBean
    private TriggerService triggerService;

    private static Media testMedia;
    private static List<Warn> testWarns;
    private static List<WarnResponseDTO> testDTOs;

    @BeforeAll
    static void initTests() {
        testMedia = new Media(1L, MediaType.MOVIE, "Media title", "Description", "/path");

        Trigger trigger = new Trigger(2L, "Name", "Description");
        TriggerDTO triggerDTO = new TriggerDTO(2L, "Name", "Description");
        User user = new User(3L, "Name", "Email", "password", "USER");

        testWarns = List.of(new Warn(4L, trigger, user, testMedia.getId(), testMedia.getType(), 5));
        testDTOs = List.of(new WarnResponseDTO(4L, testMedia.getId(), triggerDTO, 5));
    }

    @Test
    void getWarnsForMediaTest() throws Exception {
        // GIVEN
        int defaultPageIndex = 1;
        int defaultPageSize = 50;
        Long mediaId = testMedia.getId();
        MediaType mediaType = testMedia.getType();
        String uri = "/warn/%s/%s".formatted(mediaType, mediaId);
        RequestBuilder request = get(uri);

        Page<Warn> warnPage = new PageImpl<>(testWarns);
        CompletableFuture<Page<Warn>> warns = CompletableFuture.completedFuture(warnPage);

        Page<WarnResponseDTO> expectedWarns = new PageImpl<>(testDTOs);
        Pageable expectedPage = PageRequest.of(defaultPageIndex, defaultPageSize);

        when(service.getWarnsForMedia(any(), any(), any()))
                .thenReturn(warns);

        MvcResult result;
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(view().name("fragments/warns :: warnList"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("warnsUri", uri))
                .andExpect(model().attribute("warnPage", expectedWarns));

        verify(service).getWarnsForMedia(mediaId, mediaType, expectedPage);
    }

    @Test
    void getWarnsForInvalidMediaTest() throws Exception {
        // GIVEN
        long mediaId = 1234;
        String invalidMediaType = "TEST";
        String uri = "/warn/%s/%s".formatted(invalidMediaType, mediaId);

        RequestBuilder request = get(uri);
        // WHEN
        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andReturn();

        // THEN
        verify(service, never()).getWarnsForMedia(any(), any(), any());
    }

}
