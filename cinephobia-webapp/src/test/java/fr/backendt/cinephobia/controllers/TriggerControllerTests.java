package fr.backendt.cinephobia.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.backendt.cinephobia.controllers.api.v1.TriggerController;
import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.Trigger;
import fr.backendt.cinephobia.services.TriggerService;
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
@WebMvcTest(TriggerController.class)
class TriggerControllerTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private TriggerService service;

    private Trigger triggerTest;

    @BeforeEach
    void initTestValue() {
        triggerTest = new Trigger("Javaphobia", "The fear of java");
    }

    @Test
    void createTriggerTest() throws Exception {
        // GIVEN
        String triggerData = mapper.writeValueAsString(triggerTest);

        String requestUrl = "/api/v1/trigger";
        RequestBuilder request = post(requestUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(triggerData)
                .with(csrf());

        when(service.createTrigger(any()))
                .thenReturn(CompletableFuture.completedFuture(triggerTest));
        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(triggerTest));

        verify(service).createTrigger(triggerTest);
    }

    @ParameterizedTest
    @CsvSource(
            value = {
                    "    ,Description test",
                    "Name test,     ",
                    "a,Description test",
                    "Name test,a",
                    "   ,   ",
                    "a,b"
            }, ignoreLeadingAndTrailingWhitespace = false)
    void createInvalidTriggerTest(String name, String description) throws Exception {
        // GIVEN
        Trigger invalidTrigger = new Trigger(name, description);
        String triggerData = mapper.writeValueAsString(invalidTrigger);

        String requestUrl = "/api/v1/trigger";
        RequestBuilder request = post(requestUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(triggerData)
                .with(csrf());
        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isBadRequest());

        verify(service, never()).createTrigger(any());
    }

    @Test
    void createDuplicateTriggerTest() throws Exception {
        // GIVEN
        String triggerData = mapper.writeValueAsString(triggerTest);

        String requestUrl = "/api/v1/trigger";
        RequestBuilder request = post(requestUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(triggerData)
                .with(csrf());

        when(service.createTrigger(any())).thenThrow(EntityException.class);
        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isBadRequest());

        verify(service).createTrigger(triggerTest);
    }

    @Test
    void getAllTriggersTest() throws Exception {
        // GIVEN
        String requestUrl = "/api/v1/trigger";
        RequestBuilder request = get(requestUrl);

        List<Trigger> triggers = List.of(triggerTest);
        when(service.getAllTriggers())
                .thenReturn(CompletableFuture.completedFuture(triggers));
        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(triggers));

        verify(service).getAllTriggers();
    }

    @Test
    void getTriggersContainingStringTest() throws Exception {
        // GIVEN
        String triggerSearch = "java";
        String requestUrl = "/api/v1/trigger";
        RequestBuilder request = get(requestUrl)
                .param("search", triggerSearch);

        List<Trigger> triggers = List.of(triggerTest);

        when(service.getTriggersContainingString(any()))
                .thenReturn(CompletableFuture.completedFuture(triggers));
        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(triggers));


        verify(service).getTriggersContainingString(triggerSearch);
    }

    @Test
    void getTriggerByIdTest() throws Exception {
        // GIVEN
        Long triggerId = 1L;
        String requestUrl = "/api/v1/trigger/{id}";
        RequestBuilder request = get(requestUrl, triggerId);

        when(service.getTrigger(any()))
                .thenReturn(CompletableFuture.completedFuture(triggerTest));
        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(triggerTest));

        verify(service).getTrigger(triggerId);
    }

    @Test
    void getUnknownTriggerByIdTest() throws Exception {
        // GIVEN
        Long triggerId = 1L;
        String requestUrl = "/api/v1/trigger/{id}";
        RequestBuilder request = get(requestUrl, triggerId);

        when(service.getTrigger(any()))
                .thenThrow(EntityException.EntityNotFoundException.class);
        // WHEN
        mvc.perform(request)
                // THEN
                .andExpect(status().isNotFound());

        verify(service).getTrigger(triggerId);
    }

}
