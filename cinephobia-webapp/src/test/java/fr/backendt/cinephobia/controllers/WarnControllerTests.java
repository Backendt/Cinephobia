package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.exceptions.BadRequestException;
import fr.backendt.cinephobia.exceptions.EntityNotFoundException;
import fr.backendt.cinephobia.models.*;
import fr.backendt.cinephobia.models.dto.TriggerDTO;
import fr.backendt.cinephobia.models.dto.WarnDTO;
import fr.backendt.cinephobia.models.dto.WarnResponseDTO;
import fr.backendt.cinephobia.services.TriggerService;
import fr.backendt.cinephobia.services.UserService;
import fr.backendt.cinephobia.services.WarnService;
import fr.backendt.cinephobia.utils.UrlEncodedFormSerializer;
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

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser
@WebMvcTest(WarnController.class)
class WarnControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private WarnService service;

    @MockBean
    private UserService userService;

    @MockBean
    private TriggerService triggerService;

    private static Media testMedia;
    private static List<Warn> testWarns;
    private static List<WarnResponseDTO> testResponseDTOs;
    private static WarnDTO testDTO;
    private static User testUser;

    @BeforeAll
    static void initTests() {
        testMedia = new Media(1L, MediaType.MOVIE, "Media title", "Description", "/path");

        Trigger trigger = new Trigger(2L, "Name", "Description");
        TriggerDTO triggerDTO = new TriggerDTO(2L, "Name", "Description");
        testUser = new User(3L, "Name", "test@email.com", "password", "USER");

        testWarns = List.of(new Warn(4L, trigger, testUser, testMedia.getId(), testMedia.getType(), 5));
        testResponseDTOs = List.of(new WarnResponseDTO(4L, testMedia.getId(), testMedia.getType(), triggerDTO, 5));

        testDTO = new WarnDTO(4L, trigger.getId(), 5);
    }

    @Test
    void getWarnsForMediaTest() throws Exception {
        // GIVEN
        int defaultPageIndex = 0;
        int defaultPageSize = 50;
        Long mediaId = testMedia.getId();
        MediaType mediaType = testMedia.getType();
        String uri = "/warn/%s/%s".formatted(mediaType, mediaId);
        RequestBuilder request = get(uri);

        Page<Warn> warnPage = new PageImpl<>(testWarns);
        CompletableFuture<Page<Warn>> warns = completedFuture(warnPage);

        Page<WarnResponseDTO> expectedWarns = new PageImpl<>(testResponseDTOs);
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
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/warns :: warnList"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("warnsUri", uri))
                .andExpect(model().attribute("warnPage", expectedWarns));

        verify(service).getWarnsForMedia(mediaId, mediaType, expectedPage);
    }

    @WithMockUser(username = "user@test.com")
    @Test
    void getWarnsForUserTest() throws Exception {
        // GIVEN
        int defaultPageIndex = 0;
        int defaultPageSize = 50;
        String userEmail = "user@test.com";

        RequestBuilder request = get("/warns");

        Page<Warn> warnPage = new PageImpl<>(testWarns);
        CompletableFuture<Page<Warn>> warns = completedFuture(warnPage);

        Page<WarnResponseDTO> expectedWarns = new PageImpl<>(testResponseDTOs);
        Pageable expectedPage = PageRequest.of(defaultPageIndex, defaultPageSize);

        when(service.getWarnsForUser(any(), any()))
                .thenReturn(warns);

        MvcResult result;
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/warns :: profileWarns"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("warns", expectedWarns));

        verify(service).getWarnsForUser(userEmail, expectedPage);
    }

    @WithMockUser(username = "unknown@test.com")
    @Test
    void getWarnsForUnknownUserTest() throws Exception {
        // GIVEN
        int defaultPageIndex = 0;
        int defaultPageSize = 50;
        String userEmail = "unknown@test.com";

        RequestBuilder request = get("/warns");

        Page<WarnResponseDTO> expectedWarns = Page.empty();
        Pageable expectedPage = PageRequest.of(defaultPageIndex, defaultPageSize);

        when(service.getWarnsForUser(any(), any()))
                .thenReturn(failedFuture(new EntityNotFoundException("User not found")));

        MvcResult result;
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/warns :: profileWarns"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("warns", expectedWarns));

        verify(service).getWarnsForUser(userEmail, expectedPage);
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

    @WithMockUser(username = "test@email.com")
    @Test
    void createWarnTest() throws Exception {
        // GIVEN
        long triggerId = testDTO.getTriggerId();
        String userEmail = "test@email.com";

        WarnResponseDTO expectedWarnResponse = testResponseDTOs.get(0);
        Warn expectedWarn = testWarns.get(0);

        Trigger trigger = expectedWarn.getTrigger();

        String serializedWarnDTO = UrlEncodedFormSerializer.serialize(testDTO);

        String requestUri = testMedia.getMediaUri();
        RequestBuilder request = post(requestUri)
                .contentType(APPLICATION_FORM_URLENCODED)
                .content(serializedWarnDTO)
                .with(csrf());

        MvcResult result;

        when(userService.getUserByEmail(anyString(), anyBoolean())).thenReturn(completedFuture(testUser));
        when(triggerService.getTrigger(any())).thenReturn(completedFuture(trigger));
        when(service.createWarn(any())).thenReturn(completedFuture(expectedWarn));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("warn", expectedWarnResponse))
                .andExpect(view().name("fragments/warns :: warn"));

        verify(userService).getUserByEmail(userEmail, false);
        verify(triggerService).getTrigger(triggerId);
        verify(service).createWarn(expectedWarn);
    }

    @WithMockUser(username = "test@email.com")
    @Test
    void createDuplicateWarnTest() throws Exception {
        // GIVEN
        long triggerId = testDTO.getTriggerId();
        String userEmail = "test@email.com";

        Warn expectedWarn = testWarns.get(0);

        Trigger trigger = expectedWarn.getTrigger();

        String serializedWarnDTO = UrlEncodedFormSerializer.serialize(testDTO);

        String requestUri = testMedia.getMediaUri();
        RequestBuilder request = post(requestUri)
                .contentType(APPLICATION_FORM_URLENCODED)
                .content(serializedWarnDTO)
                .with(csrf());

        MvcResult result;

        when(userService.getUserByEmail(anyString(), anyBoolean())).thenReturn(completedFuture(testUser));
        when(triggerService.getTrigger(any())).thenReturn(completedFuture(trigger));
        when(service.createWarn(any())).thenReturn(failedFuture(new BadRequestException("Warn already exist")));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(header().string("HX-Retarget", "#warnForm"))
                .andExpect(header().string("HX-Reswap", "outerHTML"))
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrorCode("warn", "triggerId", "warn-already-exist"))
                .andExpect(model().attribute("warn", testDTO))
                .andExpect(view().name("fragments/warns :: warnForm"));

        verify(userService).getUserByEmail(userEmail, false);
        verify(triggerService).getTrigger(triggerId);
        verify(service).createWarn(expectedWarn);
    }

    @WithMockUser(username = "test@email.com")
    @Test
    void createWarnWithUnknownTriggerTest() throws Exception {
        // GIVEN
        long triggerId = testDTO.getTriggerId();
        String userEmail = "test@email.com";

        String serializedWarnDTO = UrlEncodedFormSerializer.serialize(testDTO);

        String requestUri = testMedia.getMediaUri();
        RequestBuilder request = post(requestUri)
                .contentType(APPLICATION_FORM_URLENCODED)
                .content(serializedWarnDTO)
                .with(csrf());

        MvcResult result;

        when(userService.getUserByEmail(anyString(), anyBoolean())).thenReturn(completedFuture(testUser));
        when(triggerService.getTrigger(any())).thenReturn(failedFuture(new EntityNotFoundException("Trigger not found")));
        when(service.createWarn(any())).thenReturn(completedFuture(null));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrorCode("warn", "triggerId", "trigger-doesnt-exist"))
                .andExpect(model().attribute("warn", testDTO))
                .andExpect(header().string("HX-Retarget", "#warnForm"))
                .andExpect(header().string("HX-Reswap", "outerHTML"))
                .andExpect(view().name("fragments/warns :: warnForm"));

        verify(userService).getUserByEmail(userEmail, false);
        verify(triggerService).getTrigger(triggerId);
        verify(service, never()).createWarn(any());
    }

    @Test
    void getWarnCreationFormTest() throws Exception {
        // GIVEN
        WarnDTO expectedBlankWarn = new WarnDTO();

        RequestBuilder request = get("/warn");
        MvcResult result;
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/warns :: warnForm"))
                .andExpect(model().attribute("warn", expectedBlankWarn))
                .andExpect(model().hasNoErrors());
    }

    @WithMockUser(username = "test@email.com")
    @Test
    void deleteWarnTest() throws Exception {
        // GIVEN
        long warnId = 1234L;
        String currentUser = "test@email.com";
        RequestBuilder request = delete("/warn/" + warnId)
                .with(csrf());

        MvcResult result;

        when(service.deleteWarnIfOwnedByUser(any(), any()))
                .thenReturn(completedFuture(null));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());

        verify(service).deleteWarnIfOwnedByUser(warnId, currentUser);
    }

    @WithMockUser(username = "test@email.com")
    @Test
    void deleteUnknownWarnTest() throws Exception {
        // GIVEN
        long warnId = 1234L;
        String currentUser = "test@email.com";
        RequestBuilder request = delete("/warn/" + warnId)
                .with(csrf());

        MvcResult result;

        when(service.deleteWarnIfOwnedByUser(any(), any()))
                .thenReturn(failedFuture(new EntityNotFoundException("Warn does not exist")));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());

        verify(service).deleteWarnIfOwnedByUser(warnId, currentUser);
    }

}
