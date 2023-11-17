package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.exceptions.BadRequestException;
import fr.backendt.cinephobia.exceptions.EntityNotFoundException;
import fr.backendt.cinephobia.models.Trigger;
import fr.backendt.cinephobia.models.dto.TriggerDTO;
import fr.backendt.cinephobia.services.TriggerService;
import fr.backendt.cinephobia.utils.UrlEncodedFormSerializer;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.List;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser
@WebMvcTest(TriggerController.class)
class TriggerControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TriggerService service;

    private Trigger trigger;
    private TriggerDTO triggerDTO;

    @BeforeEach
    void initTests() {
        trigger = new Trigger(1L, "My trigger", "Trigger description");
        triggerDTO = new TriggerDTO(1L, "My trigger", "Trigger description");
    }

    @Test
    void createTriggerTest() throws Exception {
        // GIVEN
        String triggerData = UrlEncodedFormSerializer.serialize(triggerDTO);

        RequestBuilder request = post("/admin/trigger")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(triggerData)
                .with(csrf());

        MvcResult result;

        when(service.createTrigger(any())).thenReturn(completedFuture(trigger));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/triggers :: trigger"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("trigger", triggerDTO));

        verify(service).createTrigger(trigger);
    }

    @CsvSource({
            ",My description",
            "TriggerName,",
            ",",
            "a,Name too short",
            "Description too short,a"
    })
    @ParameterizedTest
    void createInvalidTriggerTest(String name, String description) throws Exception {
        // GIVEN
        TriggerDTO invalidTrigger = new TriggerDTO(null, name, description);
        String triggerData = UrlEncodedFormSerializer.serialize(invalidTrigger);

        RequestBuilder request = post("/admin/trigger")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(triggerData)
                .with(csrf());

        MvcResult result;
        when(service.createTrigger(any())).thenReturn(completedFuture(null));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/triggers :: triggerForm"))
                .andExpect(model().hasErrors())
                .andExpect(model().attribute("trigger", invalidTrigger));

        verify(service, never()).createTrigger(any());
    }

    @Test
    void createDuplicateTriggerTest() throws Exception {
        // GIVEN
        String triggerData = UrlEncodedFormSerializer.serialize(triggerDTO);

        RequestBuilder request = post("/admin/trigger")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(triggerData)
                .with(csrf());

        MvcResult result;

        when(service.createTrigger(any())).thenReturn(failedFuture(new BadRequestException("Trigger already exists")));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/triggers :: triggerForm"))
                .andExpect(model().hasErrors())
                .andExpect(model().attribute("trigger", triggerDTO));

        verify(service).createTrigger(trigger);
    }

    @Test
    void getTriggerUpdateFormTest() throws Exception {
        // GIVEN
        long triggerId = 1L;
        RequestBuilder request = get("/admin/trigger/" + triggerId);

        MvcResult result;

        when(service.getTrigger(any())).thenReturn(completedFuture(trigger));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/triggers :: triggerForm"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("trigger", triggerDTO));

        verify(service).getTrigger(triggerId);
    }

    @Test
    void getUnknownTriggerUpdateFormTest() throws Exception {
        // GIVEN
        long triggerId = 1L;
        RequestBuilder request = get("/admin/trigger/" + triggerId);

        MvcResult result;

        when(service.getTrigger(any())).thenReturn(failedFuture(new EntityNotFoundException("Trigger not found")));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());

        verify(service).getTrigger(triggerId);
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

    @CsvSource(value = {"null","test"}, nullValues = "null")
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
    void updateTriggerTest() throws Exception {
        // GIVEN
        long triggerId = 1L;
        TriggerDTO triggerUpdate = new TriggerDTO(triggerId, "New name", null);
        String triggerData = UrlEncodedFormSerializer.serialize(triggerUpdate);

        Trigger expectedTrigger = new Trigger(triggerId, "New name", null);

        RequestBuilder request = post("/admin/trigger/" + triggerId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(triggerData)
                .with(csrf());

        MvcResult result;

        when(service.updateTrigger(any(), any())).thenReturn(completedFuture(trigger));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/triggers :: trigger"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("trigger", triggerDTO));

        verify(service).updateTrigger(triggerId, expectedTrigger);
    }

    @CsvSource({
            ",a",
            "a,"
    })
    @ParameterizedTest
    void updateInvalidTriggerTest(String name, String description) throws Exception {
        // GIVEN
        long triggerId = 1L;
        TriggerDTO invalidTrigger = new TriggerDTO(triggerId, name, description);
        String triggerData = UrlEncodedFormSerializer.serialize(invalidTrigger);

        RequestBuilder request = post("/admin/trigger/" + triggerId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(triggerData)
                .with(csrf());

        MvcResult result;
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/triggers :: triggerForm"))
                .andExpect(model().hasErrors())
                .andExpect(model().attribute("trigger", invalidTrigger));

        verify(service, never()).updateTrigger(any(), any());
    }

    @Test
    void updateDuplicateTriggerTest() throws Exception {
        // GIVEN
        long triggerId = 1L;
        TriggerDTO triggerUpdate = new TriggerDTO(triggerId, "New name", null);
        String triggerData = UrlEncodedFormSerializer.serialize(triggerUpdate);

        Trigger expectedTrigger = new Trigger(triggerId, "New name", null);

        RequestBuilder request = post("/admin/trigger/" + triggerId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(triggerData)
                .with(csrf());

        MvcResult result;

        when(service.updateTrigger(any(), any())).thenReturn(failedFuture(new BadRequestException("Trigger already exists")));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/triggers :: triggerForm"))
                .andExpect(model().hasErrors())
                .andExpect(model().attribute("trigger", triggerUpdate));

        verify(service).updateTrigger(triggerId, expectedTrigger);
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
