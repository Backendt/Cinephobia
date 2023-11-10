package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.models.Trigger;
import fr.backendt.cinephobia.services.TriggerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static fr.backendt.cinephobia.exceptions.EntityException.EntityNotFoundException;

@WithMockUser
@WebMvcTest(TriggerController.class)
class TriggerControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TriggerService service;

    private Trigger trigger;

    @BeforeEach
    void initTests() {
        trigger = new Trigger(1L, "My trigger", "Trigger description");
    }

    @Test
    void getTriggerViewTest() throws Exception {
        // GIVEN
        RequestBuilder request = get("/trigger");

        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isOk())
                .andExpect(view().name("triggers"))
                .andExpect(model().hasNoErrors());
    }

    @CsvSource({"","test"})
    @ParameterizedTest
    void getTriggersTest(String search) throws Exception {
        // GIVEN
        RequestBuilder request = get("/trigger")
                .header("Hx-Request", "true")
                .param("search", search);

        int defaultPage = 0;
        int defaultSize = 50;
        Pageable pageable = PageRequest.of(defaultPage, defaultSize);

        List<Trigger> triggers = List.of(trigger);
        Page<Trigger> triggerPage = new PageImpl<>(triggers, pageable, triggers.size());

        MvcResult result;

        when(service.getTriggers(any(), any())).thenReturn(completedFuture(triggerPage));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/triggers :: triggerList"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("triggers", triggerPage));

        verify(service).getTriggers(search, pageable);
    }

    @Test
    void getTriggersWithPageAndSizeTest() throws Exception {
        // GIVEN
        int page = 2;
        int size = 5;
        RequestBuilder request = get("/trigger")
                .header("Hx-Request", "true")
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size));

        Pageable pageable = PageRequest.of(page, size);

        List<Trigger> triggers = List.of(trigger);
        Page<Trigger> triggerPage = new PageImpl<>(triggers, pageable, triggers.size());

        MvcResult result;

        when(service.getTriggers(any(), any())).thenReturn(completedFuture(triggerPage));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/triggers :: triggerList"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("triggers", triggerPage));

        verify(service).getTriggers(null, pageable);
    }

    @Test
    void deleteTriggerTest() throws Exception {
        // GIVEN
        long triggerId = 1L;
        RequestBuilder request = delete("/admin/trigger/" + triggerId)
                .with(csrf());

        MvcResult result;

        when(service.deleteTrigger(any())).thenReturn(completedFuture(null));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());

        verify(service).deleteTrigger(triggerId);
    }

    @Test
    void deleteUnknownTriggerTest() throws Exception {
        // GIVEN
        long triggerId = 1L;
        RequestBuilder request = delete("/admin/trigger/" + triggerId)
                .with(csrf());

        MvcResult result;

        when(service.deleteTrigger(any()))
                .thenReturn(failedFuture(new EntityNotFoundException("Trigger not found")));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());

        verify(service).deleteTrigger(triggerId);
    }
}
