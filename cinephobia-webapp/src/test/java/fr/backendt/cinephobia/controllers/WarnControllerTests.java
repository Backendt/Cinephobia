package fr.backendt.cinephobia.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.backendt.cinephobia.controllers.api.v1.WarnController;
import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.mappers.WarnMapper;
import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.models.Trigger;
import fr.backendt.cinephobia.models.Warn;
import fr.backendt.cinephobia.models.dto.WarnDTO;
import fr.backendt.cinephobia.services.WarnService;
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
@WebMvcTest(WarnController.class)
class WarnControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private WarnService service;

    @MockBean
    private WarnMapper mapper;

    private WarnDTO warnDtoTest;
    private Warn warnTest;

    @BeforeEach
    void initTests() {
        Media media = new Media(1L, null, null, null);
        Trigger trigger = new Trigger(1L, null, null);
        warnTest = new Warn(1L, trigger, media, 9);
        warnDtoTest = new WarnDTO(1L, 1L, 9);

        when(mapper.toDTO(warnTest)).thenReturn(warnDtoTest);
        when(mapper.toEntity(warnDtoTest)).thenReturn(warnTest);
        when(mapper.toDTOs(List.of(warnTest))).thenReturn(List.of(warnDtoTest));
    }

    @Test
    void createWarnTest() throws Exception {
        // GIVEN
        String warnData = new ObjectMapper().writeValueAsString(warnDtoTest);

        String requestUrl = "/api/v1/warn";
        RequestBuilder request = post(requestUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(warnData)
                .with(csrf());

        when(service.createWarn(any()))
                .thenReturn(CompletableFuture.completedFuture(warnTest));
        // WHEN
        mvc.perform(request)
                // THEN
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(warnDtoTest));

        verify(service).createWarn(warnTest);
    }

    @ParameterizedTest
    @CsvSource({
            ",1,5",
            "1,,5",
            "1,1,0",
            "1,1,-5",
            "1,1,11"
    })
    void createInvalidWarnTest(Long triggerId, Long mediaId, int expositionLevel) throws Exception {
        // GIVEN
        WarnDTO invalidWarn = new WarnDTO(triggerId, mediaId, expositionLevel);
        String warnData = new ObjectMapper().writeValueAsString(invalidWarn);

        String requestUrl = "/api/v1/warn";
        RequestBuilder request = post(requestUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(warnData)
                .with(csrf());
        // WHEN
        mvc.perform(request)
                // THEN
                .andExpect(status().isBadRequest());

        verify(service, never()).createWarn(any());
    }

    @Test
    void getAllWarnsTest() throws Exception {
        // GIVEN
        String requestUrl = "/api/v1/warn";
        RequestBuilder request = get(requestUrl);

        List<Warn> warns = List.of(warnTest);
        List<WarnDTO> expected = List.of(warnDtoTest);
        when(service.getAllWarns())
                .thenReturn(CompletableFuture.completedFuture(warns));
        // WHEN
        mvc.perform(request)
                // THEN
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(expected));

        verify(service).getAllWarns();
    }

    @Test
    void getWarnByIdTest() throws Exception {
        // GIVEN
        Long warnId = 1L;
        String requestUrl = "/api/v1/warn/{id}";
        RequestBuilder request = get(requestUrl, warnId);

        when(service.getWarn(any()))
                .thenReturn(CompletableFuture.completedFuture(warnTest));
        // WHEN
        mvc.perform(request)
                // THEN
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(warnDtoTest));

        verify(service).getWarn(warnId);
    }

    @Test
    void getUnknownWarnByIdTest() throws Exception {
        // GIVEN
        Long warnId = 1L;
        String requestUrl = "/api/v1/warn/{id}";
        RequestBuilder request = get(requestUrl, warnId);

        when(service.getWarn(any()))
                .thenThrow(EntityException.EntityNotFoundException.class);
        // WHEN
        mvc.perform(request)
                // THEN
                .andExpect(status().isNotFound());

        verify(service).getWarn(warnId);
    }

}
