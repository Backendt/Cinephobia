package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.exceptions.BadRequestException;
import fr.backendt.cinephobia.exceptions.EntityNotFoundException;
import fr.backendt.cinephobia.models.Trigger;
import fr.backendt.cinephobia.models.User;
import fr.backendt.cinephobia.models.dto.ProfileResponseDTO;
import fr.backendt.cinephobia.models.dto.UserResponseDTO;
import fr.backendt.cinephobia.models.dto.TriggerDTO;
import fr.backendt.cinephobia.models.dto.UserDTO;
import fr.backendt.cinephobia.services.TriggerService;
import fr.backendt.cinephobia.services.UserService;
import fr.backendt.cinephobia.utils.UrlEncodedFormSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.List;
import java.util.Set;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser
@WebMvcTest(UserController.class)
class UserControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService service;

    @MockBean
    private TriggerService triggerService;

    @MockBean
    private SessionRegistry sessions;
    private UserDetails principal;
    private SessionInformation session;

    private static List<User> userList;
    private static List<UserResponseDTO> dtoList;

    @BeforeAll
    static void initUsers() {
        userList = List.of(
                new User(1L, "User One", "user.one@test.com", "Password", "USER"),
                new User(2L, "User Two", "user.two@test.com", "Password", "USER"),
                new User(3L, "User Three", "user.three@test.com", "Password", "ADMIN")
        );

        dtoList = List.of(
                new UserResponseDTO(1L, "User One", "user.one@test.com", "Password", "USER"),
                new UserResponseDTO(2L, "User Two", "user.two@test.com", "Password", "USER"),
                new UserResponseDTO(3L, "User Three", "user.three@test.com", "Password", "ADMIN")
        );
    }

    @BeforeEach
    void initTests() {
        principal = Mockito.mock(UserDetails.class);
        session = Mockito.mock(SessionInformation.class);

        when(sessions.getAllPrincipals()).thenReturn(List.of(principal));
        when(sessions.getAllSessions(any(), anyBoolean())).thenReturn(List.of(session));
    }

    @Test
    void getUsersViewTest() throws Exception {
        // GIVEN
        RequestBuilder request = get("/admin/user");

        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().hasNoErrors());
    }

    @CsvSource({
            "0,50,0,50",
            "1,100,1,100",
            "0,1,0,1",
            "-1,50,0,50",
            "0,0,0,1",
            "0,600,0,500"
    })
    @ParameterizedTest
    void getUsersTest(Integer pageIndex, Integer pageSize, Integer expectedIndex, Integer expectedSize) throws Exception {
        // GIVEN
        RequestBuilder request = get("/admin/user")
                .header("Hx-Request", "true")
                .param("page", String.valueOf(pageIndex))
                .param("size", String.valueOf(pageSize));

        Page<User> userPage = new PageImpl<>(userList);
        Page<UserResponseDTO> expectedPage = new PageImpl<>(dtoList);

        Pageable expectedPageRequest = PageRequest.of(expectedIndex, expectedSize);
        MvcResult result;

        when(service.getUsers(any(), any()))
                .thenReturn(completedFuture(userPage));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/users :: userList"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("users", expectedPage));

        verify(service).getUsers(null, expectedPageRequest);
    }

    @Test
    void getUsersWithSearchTest() throws Exception {
        // GIVEN
        String nameSearch = "test search";
        RequestBuilder request = get("/admin/user")
                .header("Hx-Request", "true")
                .param("search", nameSearch);

        Pageable defaultPageRequest = PageRequest.of(0, 50);

        MvcResult result;

        Page<User> userPage = new PageImpl<>(userList);
        when(service.getUsers(any(), any()))
                .thenReturn(completedFuture(userPage));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/users :: userList"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attributeExists("users"));

        verify(service).getUsers(nameSearch, defaultPageRequest);
    }

    @Test
    void getUserEditFormTest() throws Exception {
        // GIVEN
        long userId = 1L;
        RequestBuilder request = get("/admin/user/" + userId);

        User user = userList.get(0);
        UserResponseDTO userDTO = dtoList.get(0);

        MvcResult result;
        when(service.getUserById(any())).thenReturn(completedFuture(user));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/users :: userForm"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("user", userDTO));

        verify(service).getUserById(userId);
    }

    @Test
    void getUnknownUserEditFormTest() throws Exception {
        // GIVEN
        long userId = 1L;
        RequestBuilder request = get("/admin/user/" + userId);

        MvcResult result;
        when(service.getUserById(any())).thenReturn(
                failedFuture(new EntityNotFoundException("User not found"))
        );
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());

        verify(service).getUserById(userId);
    }

    @Test
    void updateUserTest() throws Exception {
        // GIVEN
        long userId = 1L;

        User user = userList.get(0);
        UserResponseDTO userDto = dtoList.get(0);

        UserDTO userUpdate = new UserDTO("Updated", null, null);
        User userUpdateEntity = new User(null, "Updated", null, null, null);
        String userUpdateData = UrlEncodedFormSerializer.serialize(userUpdate);

        RequestBuilder request = post("/admin/user/" + userId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(userUpdateData)
                .with(csrf());

        MvcResult result;

        when(service.updateUserById(any(), any())).thenReturn(completedFuture(user));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/users :: user"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("user", userDto));

        verify(service).updateUserById(userId, userUpdateEntity);
    }

    @Test
    void updateInvalidUserTest() throws Exception {
        // GIVEN
        long userId = 1L;

        User user = userList.get(0);

        UserDTO userUpdate = new UserDTO("a", null, null);
        String userUpdateData = UrlEncodedFormSerializer.serialize(userUpdate);

        RequestBuilder request = post("/admin/user/" + userId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(userUpdateData)
                .with(csrf());

        MvcResult result;

        when(service.updateUserById(any(), any())).thenReturn(completedFuture(user));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/users :: userForm"))
                .andExpect(model().hasErrors())
                .andExpect(model().attribute("user", userUpdate));

        verify(service, never()).updateUserById(any(), any());
    }

    @Test
    void updateUnknownUserTest() throws Exception {
        // GIVEN
        long userId = 1L;

        UserDTO userUpdate = new UserDTO("Updated", null, null);
        User userUpdateEntity = new User(null, "Updated", null, null, null);
        String userUpdateData = UrlEncodedFormSerializer.serialize(userUpdate);

        RequestBuilder request = post("/admin/user/" + userId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(userUpdateData)
                .with(csrf());

        MvcResult result;

        when(service.updateUserById(any(), any()))
                .thenReturn(failedFuture(new EntityNotFoundException("User not found")));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());

        verify(service).updateUserById(userId, userUpdateEntity);
    }

    @Test
    void makeUserAdminTest() throws Exception {
        // GIVEN
        long userId = 1L;
        RequestBuilder request = post("/admin/user/role/" + userId)
                .with(csrf());

        User userUpdate = new User();
        userUpdate.setRole("ADMIN");

        User user = userList.get(0);
        UserResponseDTO updatedUserDTO = dtoList.get(0);

        MvcResult result;
        when(service.updateUserById(any(), any())).thenReturn(completedFuture(user));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/users :: user"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("user", updatedUserDTO));

        verify(service).updateUserById(userId, userUpdate);
    }

    @Test
    void makeUnknownUserAdminTest() throws Exception {
        // GIVEN
        long userId = 1L;
        RequestBuilder request = post("/admin/user/role/" + userId)
                .with(csrf());

        User userUpdate = new User();
        userUpdate.setRole("ADMIN");

        MvcResult result;
        when(service.updateUserById(any(), any())).thenReturn(
                failedFuture(new EntityNotFoundException("User not found"))
        );
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());

        verify(service).updateUserById(userId, userUpdate);
    }

    @WithMockUser(username = "user@test.com")
    @Test
    void getUserProfileTest() throws Exception {
        // GIVEN
        String loggedInUserEmail = "user@test.com";

        Set<Trigger> triggers = Set.of(new Trigger(1L, "Trigger name", "Trigger description"));
        Set<TriggerDTO> triggersDTO = Set.of(new TriggerDTO(1L, "Trigger name", "Trigger description"));

        User user = userList.get(0);
        user.setTriggers(triggers);

        ProfileResponseDTO expectedUserDTO = new ProfileResponseDTO(1L, "User One", "user.one@test.com", "Password", "USER", triggersDTO);

        RequestBuilder request = get("/profile");
        MvcResult result;

        when(service.getUserByEmail(any(), anyBoolean()))
                .thenReturn(completedFuture(user));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("user", expectedUserDTO));

        verify(service).getUserByEmail(loggedInUserEmail, true);
    }

    @WithMockUser(username = "expired.user@test.com")
    @Test
    void getExpiredUserProfileTest() throws Exception {
        // GIVEN
        String loggedInUserEmail = "expired.user@test.com";

        RequestBuilder request = get("/profile");
        MvcResult result;

        when(service.getUserByEmail(any(), anyBoolean()))
                .thenReturn(failedFuture(new EntityNotFoundException("User not found")));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));

        verify(service).getUserByEmail(loggedInUserEmail, true);
    }

    @WithMockUser(username = "current.user@test.com")
    @Test
    void getUserProfileEditFormTest() throws Exception {
        // GIVEN
        String userEmail = "current.user@test.com";
        RequestBuilder request = get("/profile")
                .header("Hx-Request", "true");

        User user = userList.get(0);
        UserResponseDTO userDto = dtoList.get(0);

        MvcResult result;

        when(service.getUserByEmail(any(), anyBoolean())).thenReturn(completedFuture(user));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/users :: profileForm"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("user", userDto));

        verify(service).getUserByEmail(userEmail, false);
    }

    @WithMockUser(username = "unknown.user@test.com")
    @Test
    void getUnknownUserProfileEditFormTest() throws Exception {
        // GIVEN
        String userEmail = "unknown.user@test.com";
        RequestBuilder request = get("/profile")
                .header("Hx-Request", "true");

        MvcResult result;

        when(service.getUserByEmail(any(), anyBoolean())).thenReturn(
                failedFuture(new EntityNotFoundException("User not found"))
        );
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));

        verify(service).getUserByEmail(userEmail, false);
    }

    @WithMockUser(username = "current.user@test.com")
    @Test
    void updateUserProfileTest() throws Exception {
        // GIVEN
        String currentUserEmail = "current.user@test.com";

        UserDTO userUpdate = new UserDTO("My new name", null, null);
        User userUpdateEntity = new User(null, "My new name", null, null, null);
        String userUpdateData = UrlEncodedFormSerializer.serialize(userUpdate);

        User user = userList.get(0);
        UserResponseDTO expectedUser = dtoList.get(0);

        RequestBuilder request = post("/profile")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(userUpdateData)
                .with(csrf());

        MvcResult result;

        when(service.updateUserByEmail(any(), any())).thenReturn(completedFuture(user));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/users :: profile"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("user", expectedUser));

        verify(service).updateUserByEmail(currentUserEmail, userUpdateEntity);
    }

    @WithMockUser(username = "current.user@test.com")
    @CsvSource({
            "a,email@test.com,myPassword,1",
            "aaa\0aaa,email@test.com,myPassword,1",
            "Username,notanemail,myPassword,1",
            "a,notanemail,,2",
            ",,pass,1"
    })
    @ParameterizedTest
    void updateInvalidUserProfileTest(String displayName, String email, String password, Integer expectedErrorAmount) throws Exception {
        // GIVEN
        UserDTO invalidUpdate = new UserDTO(displayName, email, password);
        String invalidUpdateData = UrlEncodedFormSerializer.serialize(invalidUpdate);

        RequestBuilder request = post("/profile")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(invalidUpdateData)
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
                .andExpect(view().name("fragments/users :: profileForm"))
                .andExpect(model().errorCount(expectedErrorAmount))
                .andExpect(model().attribute("user", invalidUpdate));

        verify(service, never()).updateUserByEmail(any(), any());
    }

    @WithMockUser(username = "expired.user@test.com")
    @Test
    void updateExpiredUserProfileTest() throws Exception {
        // GIVEN
        String currentUserEmail = "expired.user@test.com";

        UserDTO userUpdate = new UserDTO("My new name", null, null);
        User userUpdateEntity = new User(null, "My new name", null, null, null);
        String userUpdateData = UrlEncodedFormSerializer.serialize(userUpdate);

        RequestBuilder request = post("/profile")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(userUpdateData)
                .with(csrf());

        MvcResult result;

        when(service.updateUserByEmail(any(), any()))
                .thenReturn(failedFuture(new EntityNotFoundException("User not found")));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));

        verify(service).updateUserByEmail(currentUserEmail, userUpdateEntity);
    }

    @WithMockUser(username = "current.user@test.com")
    @Test
    void updateUserProfileToTakenEmailTest() throws Exception {
        // GIVEN
        String currentUserEmail = "current.user@test.com";
        String newEmail = "new.email@test.com";

        User userUpdateEntity = new User(null, "My new name", newEmail, null, null);
        UserDTO userUpdate = new UserDTO("My new name", newEmail, null);
        String userUpdateData = UrlEncodedFormSerializer.serialize(userUpdate);

        RequestBuilder request = post("/profile")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(userUpdateData)
                .with(csrf());

        MvcResult result;

        when(service.updateUserByEmail(any(), any()))
                .thenReturn(failedFuture(new BadRequestException("Email already taken")));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/users :: profileForm"))
                .andExpect(model().attributeHasFieldErrorCode("user", "email", "email-taken"))
                .andExpect(model().attribute("user", userUpdate));

        verify(service).updateUserByEmail(currentUserEmail, userUpdateEntity);
    }

    @WithMockUser(username = "user@test.com")
    @Test
    void getUserTriggersTest() throws Exception {
        // GIVEN
        Trigger trigger = new Trigger(1L, "TriggerTest", "TriggerDesc");
        TriggerDTO triggerDTO = new TriggerDTO(1L, "TriggerTest", "TriggerDesc");

        String userEmail = "user@test.com";
        User currentUser = new User(userList.get(0));
        currentUser.setEmail(userEmail);
        currentUser.setTriggers(Set.of(trigger));

        RequestBuilder request = get("/profile/triggers");
        MvcResult result;

        when(service.getUserByEmail(any(), anyBoolean())).thenReturn(completedFuture(currentUser));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("triggers", Set.of(triggerDTO)))
                .andExpect(view().name("fragments/triggers :: triggersSelection"));

        verify(service).getUserByEmail(userEmail, true);
    }

    @WithMockUser(username = "user@test.com")
    @Test
    void getUnknownUserTriggersTest() throws Exception {
        // GIVEN
        String userEmail = "user@test.com";

        RequestBuilder request = get("/profile/triggers");
        MvcResult result;

        when(service.getUserByEmail(any(), anyBoolean()))
                .thenReturn(failedFuture(new EntityNotFoundException("User not found")));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isFound())
                .andExpect(model().hasNoErrors())
                .andExpect(redirectedUrl("/login"));

        verify(service).getUserByEmail(userEmail, true);
    }

    @WithMockUser(username = "user@test.com")
    @Test
    void addTriggerToProfileTest() throws Exception {
        // GIVEN
        String userEmail = "user@test.com";
        long triggerId = 1L;

        Trigger trigger = new Trigger(1L, "Test trigger", "Test trigger");

        RequestBuilder request = post("/profile/trigger")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", String.valueOf(triggerId))
                .with(csrf());

        MvcResult result;

        when(triggerService.getTrigger(any())).thenReturn(completedFuture(trigger));
        when(service.addTriggerToUser(any(), any()))
                .thenReturn(completedFuture(null));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());

        verify(triggerService).getTrigger(triggerId);
        verify(service).addTriggerToUser(userEmail, trigger);
    }

    @WithMockUser(username = "user@test.com")
    @Test
    void addUnknownTriggerToProfileTest() throws Exception {
        // GIVEN
        long triggerId = 1L;

        RequestBuilder request = post("/profile/trigger")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", String.valueOf(triggerId))
                .with(csrf());

        MvcResult result;

        when(triggerService.getTrigger(any()))
                .thenReturn(failedFuture(new EntityNotFoundException("Trigger not found")));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());

        verify(triggerService).getTrigger(triggerId);
        verify(service, never()).addTriggerToUser(any(), any());
    }

    @WithMockUser(username = "user@test.com")
    @Test
    void addAlreadyPresentTriggerToProfileTest() throws Exception {
        // GIVEN
        String userEmail = "user@test.com";
        long triggerId = 1L;

        Trigger trigger = new Trigger(1L, "Test trigger", "Test trigger");

        RequestBuilder request = post("/profile/trigger")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", String.valueOf(triggerId))
                .with(csrf());

        MvcResult result;

        when(triggerService.getTrigger(any())).thenReturn(completedFuture(trigger));
        when(service.addTriggerToUser(any(), any()))
                .thenReturn(failedFuture(new BadRequestException("User already have trigger")));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest());

        verify(triggerService).getTrigger(triggerId);
        verify(service).addTriggerToUser(userEmail, trigger);
    }

    @WithMockUser(username = "user@test.com")
    @Test
    void addTriggerToUnknownProfileTest() throws Exception {
        // GIVEN
        String userEmail = "user@test.com";
        long triggerId = 1L;

        Trigger trigger = new Trigger(1L, "Test trigger", "Test trigger");

        RequestBuilder request = post("/profile/trigger")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", String.valueOf(triggerId))
                .with(csrf());

        MvcResult result;

        when(triggerService.getTrigger(any())).thenReturn(completedFuture(trigger));
        when(service.addTriggerToUser(any(), any()))
                .thenReturn(failedFuture(new EntityNotFoundException("User not found")));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());

        verify(triggerService).getTrigger(triggerId);
        verify(service).addTriggerToUser(userEmail, trigger);
    }

    @WithMockUser(username = "user@test.com")
    @Test
    void removeTriggerFromProfileTest() throws Exception {
        // GIVEN
        String userEmail = "user@test.com";
        long triggerId = 1L;
        RequestBuilder request = delete("/profile/trigger/" + triggerId)
                .with(csrf());

        MvcResult result;

        when(service.removeTriggerFromUser(any(), any()))
                .thenReturn(completedFuture(null));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());

        verify(service).removeTriggerFromUser(userEmail, triggerId);
    }

    @WithMockUser(username = "user@test.com")
    @Test
    void removeTriggerFromUnknownProfileTest() throws Exception {
        // GIVEN
        String userEmail = "user@test.com";
        long triggerId = 1L;
        RequestBuilder request = delete("/profile/trigger/" + triggerId)
                .with(csrf());

        MvcResult result;

        when(service.removeTriggerFromUser(any(), any()))
                .thenReturn(failedFuture(new EntityNotFoundException("User not found")));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());

        verify(service).removeTriggerFromUser(userEmail, triggerId);
    }

    @WithMockUser(username = "user@test.com")
    @Test
    void removeUnknownTriggerFromProfileTest() throws Exception {
        // GIVEN
        String userEmail = "user@test.com";
        long triggerId = 1L;
        RequestBuilder request = delete("/profile/trigger/" + triggerId)
                .with(csrf());

        MvcResult result;

        when(service.removeTriggerFromUser(any(), any()))
                .thenReturn(failedFuture(new BadRequestException("User doesn't have trigger")));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest());

        verify(service).removeTriggerFromUser(userEmail, triggerId);
    }

    @WithMockUser(username = "user@test.com")
    @Test
    void deleteUserProfileTest() throws Exception {
        // GIVEN
        String userEmail = "user@test.com";
        RequestBuilder request = delete("/profile")
                .with(csrf());

        when(service.deleteUserByEmail(any())).thenReturn(completedFuture(null));
        // WHEN
        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(header().string("Hx-Redirect", "/"));

        verify(sessions).getAllSessions(any(), eq(false));
        verify(session).expireNow();

        verify(service).deleteUserByEmail(userEmail);
    }

    @WithMockUser(username = "user@test.com")
    @Test
    void deleteExpiredUserProfileTest() throws Exception {
        // GIVEN
        String userEmail = "user@test.com";
        RequestBuilder request = delete("/profile")
                .with(csrf());

        when(service.deleteUserByEmail(any()))
                .thenReturn(
                        failedFuture(new EntityNotFoundException("User not found"))
                );
        // WHEN
        mvc.perform(request)
                .andExpect(status().isNotFound());

        verify(sessions).getAllSessions(any(), eq(false));
        verify(session).expireNow();

        verify(service).deleteUserByEmail(userEmail);
    }

    @Test
    void deleteUserTest() throws Exception {
        // GIVEN
        long userId = 1L;
        String userEmail = "user@test.com";

        RequestBuilder request = delete("/admin/user/" + userId)
                .with(csrf());

        when(service.getUserEmailById(any())).thenReturn(completedFuture(userEmail));
        when(principal.getUsername()).thenReturn(userEmail);
        when(service.deleteUserById(any())).thenReturn(completedFuture(null));
        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isOk())
                .andExpect(header().string("Hx-Redirect", "/admin/user"));

        verify(service).getUserEmailById(userId);
        verify(sessions).getAllPrincipals();
        verify(principal).getUsername();
        verify(sessions).getAllSessions(principal, false);
        verify(session).expireNow();

        verify(service).deleteUserById(userId);
    }

    @Test
    void deleteUnknownUserTest() throws Exception {
        // GIVEN
        long userId = 1L;

        RequestBuilder request = delete("/admin/user/" + userId)
                .with(csrf());

        when(service.getUserEmailById(any()))
                .thenReturn(failedFuture(new EntityNotFoundException("User not found"))); // This error is ignored in controller

        when(service.deleteUserById(any()))
                .thenReturn(failedFuture(new EntityNotFoundException("User not found")));
        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isNotFound());

        verify(service).deleteUserById(userId);
    }

}
