package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.User;
import fr.backendt.cinephobia.models.dto.FullUserDTO;
import fr.backendt.cinephobia.models.dto.UserDTO;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
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
    private SessionRegistry sessions;
    private UserDetails principal;
    private SessionInformation session;

    private static List<User> userList;
    private static List<FullUserDTO> dtoList;

    @BeforeAll
    static void initUsers() {
        userList = List.of(
                new User(1L, "User One", "user.one@test.com", "Password", "USER"),
                new User(2L, "User Two", "user.two@test.com", "Password", "USER"),
                new User(3L, "User Three", "user.three@test.com", "Password", "ADMIN")
        );

        dtoList = List.of(
                new FullUserDTO(1L, "User One", "user.one@test.com", "Password", "USER"),
                new FullUserDTO(2L, "User Two", "user.two@test.com", "Password", "USER"),
                new FullUserDTO(3L, "User Three", "user.three@test.com", "Password", "ADMIN")
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
    void getUsersTest() throws Exception {
        // GIVEN
        RequestBuilder request = get("/admin/user");

        MvcResult result;

        when(service.getUsers())
                .thenReturn(completedFuture(userList));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("users", dtoList));

        verify(service).getUsers();
        verify(service, never()).getUserByEmail(any());
    }

    @Test
    void getUserWithEmailTest() throws Exception {
        // GIVEN
        String userEmail = "user@test.com";
        User user = userList.get(0);
        FullUserDTO userDto = dtoList.get(0);

        RequestBuilder request = get("/admin/user")
                .param("email", userEmail);

        MvcResult result;

        when(service.getUserByEmail(any())).thenReturn(completedFuture(user));
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
                .andExpect(model().attribute("user", userDto));

        verify(service).getUserByEmail(userEmail);
        verify(service, never()).getUsers();
    }

    @Test
    void updateUserTest() throws Exception {
        // GIVEN
        long userId = 1L;

        User user = userList.get(0);
        FullUserDTO userDto = dtoList.get(0);

        FullUserDTO userUpdate = new FullUserDTO(null, "Updated", null, null, null);
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
                .andExpect(status().isCreated())
                .andExpect(view().name("profile"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("user", userDto));

        verify(service).updateUserById(userId, userUpdateEntity);
    }

    @Test
    void updateUnknownUserTest() throws Exception {
        // GIVEN
        long userId = 1L;

        FullUserDTO userUpdate = new FullUserDTO(null, "Updated", null, null, null);
        User userUpdateEntity = new User(null, "Updated", null, null, null);
        String userUpdateData = UrlEncodedFormSerializer.serialize(userUpdate);

        RequestBuilder request = post("/admin/user/" + userId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(userUpdateData)
                .with(csrf());

        MvcResult result;

        when(service.updateUserById(any(), any()))
                .thenReturn(failedFuture(new EntityException.EntityNotFoundException("User not found")));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("profile"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("user", userUpdate));

        verify(service).updateUserById(userId, userUpdateEntity);
    }

    @WithMockUser(username = "expired.user@test.com")
    @Test
    void getUserProfileTest() throws Exception {
        // GIVEN
        String loggedInUserEmail = "expired.user@test.com";

        RequestBuilder request = get("/profile");
        MvcResult result;

        when(service.getUserByEmail(any()))
                .thenReturn(failedFuture(new EntityException.EntityNotFoundException("User not found")));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));

        verify(service).getUserByEmail(loggedInUserEmail);
    }

    @WithMockUser(username = "current.user@test.com")
    @Test
    void updateUserProfileTest() throws Exception {
        // GIVEN
        String currentUserEmail = "current.user@test.com";
        long userId = 1L;
        UserDTO userUpdate = new UserDTO("My new name", null, null);
        User userUpdateEntity = new User(null, "My new name", null, null, null);
        String userUpdateData = UrlEncodedFormSerializer.serialize(userUpdate);

        User user = userList.get(0);
        FullUserDTO expectedUser = dtoList.get(0);

        RequestBuilder request = post("/profile")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(userUpdateData)
                .with(csrf());

        MvcResult result;

        when(service.getUserIdByEmail(any())).thenReturn(completedFuture(userId));
        when(service.updateUserById(any(), any())).thenReturn(completedFuture(user));
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
                .andExpect(model().attribute("user", expectedUser));

        verify(service).getUserIdByEmail(currentUserEmail);
        verify(service).updateUserById(userId, userUpdateEntity);
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
                .andExpect(view().name("profile"))
                .andExpect(model().errorCount(expectedErrorAmount))
                .andExpect(model().attribute("user", invalidUpdate));

        verify(service, never()).getUserById(any());
        verify(service, never()).updateUserById(any(), any());
    }

    @WithMockUser(username = "expired.user@test.com")
    @Test
    void updateExpiredUserProfileTest() throws Exception {
        // GIVEN
        String currentUserEmail = "expired.user@test.com";
        UserDTO userUpdate = new UserDTO("My new name", null, null);
        String userUpdateData = UrlEncodedFormSerializer.serialize(userUpdate);

        RequestBuilder request = post("/profile")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(userUpdateData)
                .with(csrf());

        MvcResult result;

        when(service.getUserIdByEmail(any()))
                .thenReturn(failedFuture(new EntityException.EntityNotFoundException("User not found")));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));

        verify(service).getUserIdByEmail(currentUserEmail);
        verify(service, never()).updateUserById(any(), any());
    }

    @WithMockUser(username = "current.user@test.com")
    @Test
    void updateUnknownUserProfileTest() throws Exception {
        // GIVEN
        String currentUserEmail = "current.user@test.com";
        long userId = 1L;
        UserDTO userUpdate = new UserDTO("My new name", null, null);
        User userUpdateEntity = new User(null, "My new name", null, null, null);
        String userUpdateData = UrlEncodedFormSerializer.serialize(userUpdate);

        RequestBuilder request = post("/profile")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(userUpdateData)
                .with(csrf());

        MvcResult result;

        when(service.getUserIdByEmail(any())).thenReturn(completedFuture(userId));
        when(service.updateUserById(any(), any()))
                .thenReturn(failedFuture(new EntityException("Email already taken")));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeHasFieldErrorCode("user", "email", "email-taken"))
                .andExpect(model().attribute("user", userUpdate));

        verify(service).getUserIdByEmail(currentUserEmail);
        verify(service).updateUserById(userId, userUpdateEntity);
    }


    @WithMockUser(username = "user@test.com")
    @Test
    void deleteUserProfileTest() throws Exception {
        // GIVEN
        long userId = 1L;
        String userEmail = "user@test.com";
        RequestBuilder request = delete("/profile")
                .with(csrf());

        when(service.getUserIdByEmail(any()))
                .thenReturn(completedFuture(userId));
        // WHEN
        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(header().string("Hx-Redirect", "/"));

        verify(sessions).getAllSessions(any(), eq(false));
        verify(session).expireNow();

        verify(service).getUserIdByEmail(userEmail);
        verify(service).deleteUserById(userId);
    }

    @WithMockUser(username = "user@test.com")
    @Test
    void deleteExpiredUserProfileTest() throws Exception {
        // GIVEN
        long userId = 1L;
        String userEmail = "user@test.com";
        RequestBuilder request = delete("/profile")
                .with(csrf());

        when(service.getUserIdByEmail(any()))
                .thenReturn(
                        failedFuture(new EntityException.EntityNotFoundException("User not found"))
                );
        // WHEN
        mvc.perform(request)
                .andExpect(status().isNotFound());

        verify(sessions).getAllSessions(any(), eq(false));
        verify(session).expireNow();

        verify(service).getUserIdByEmail(userEmail);
        verify(service, never()).deleteUserById(userId);
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
                .thenReturn(failedFuture(new EntityException.EntityNotFoundException("User not found"))); // This error is ignored in controller

        when(service.deleteUserById(any()))
                .thenReturn(failedFuture(new EntityException.EntityNotFoundException("User not found")));
        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isNotFound());

        verify(service).deleteUserById(userId);
    }

}
