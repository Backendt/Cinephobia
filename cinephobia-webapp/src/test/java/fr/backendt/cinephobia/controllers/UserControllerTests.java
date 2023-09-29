package fr.backendt.cinephobia.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.backendt.cinephobia.controllers.api.v1.UserController;
import fr.backendt.cinephobia.exceptions.EntityException;
import fr.backendt.cinephobia.models.User;
import fr.backendt.cinephobia.models.dto.UserDTO;
import fr.backendt.cinephobia.models.dto.UserResponseDTO;
import fr.backendt.cinephobia.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(username = UserControllerTests.USER_ID)
@WebMvcTest(UserController.class)
class UserControllerTests {

    static final String USER_ID = "1";

    private static final String USER_ENDPOINT = "/api/v1/user";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService service;

    @Autowired
    private ObjectMapper mapper;

    private User user;
    private UserDTO userDTO;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void initTests() {
        long userId = 1L;
        String name = "TestUser";
        String password = "MyPassword";
        String email = "user@test.com";
        user = new User(userId, name, email, password, "USER");
        userDTO = new UserDTO(name, email, password);
        userResponseDTO = new UserResponseDTO(userId, name, email);
    }

    /*
    ADMIN ONLY
     */

    @WithMockUser(roles = "ADMIN")
    @Test
    void getUserByIdTest() throws Exception {
        // GIVEN
        long userId = 1L;
        String adminEndpoint = USER_ENDPOINT + '/' + userId;
        RequestBuilder request = get(adminEndpoint);

        when(service.getUserById(any()))
                .thenReturn(CompletableFuture.completedFuture(user));
        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(userResponseDTO));
        
        verify(service).getUserById(userId);
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void getUnknownUserByIdTest() throws Exception {
        // GIVEN
        long userId = 1L;
        String adminEndpoint = USER_ENDPOINT + '/' + userId;
        RequestBuilder request = get(adminEndpoint);

        when(service.getUserById(any()))
                .thenThrow(EntityException.EntityNotFoundException.class);
        // WHEN
        mvc.perform(request)
                // THEN
                .andExpect(status().isNotFound());

        verify(service).getUserById(userId);
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void updateUserByIdTest() throws Exception {
        // GIVEN
        UserDTO userPatch = new UserDTO("New Name", null, null);
        String userPatchData = mapper.writeValueAsString(userPatch);

        User userPatchEntity = new User(null, "New Name", null, null, null);

        long userId = 1L;
        String adminEndpoint = USER_ENDPOINT + '/' + userId;
        RequestBuilder request = patch(adminEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPatchData)
                .with(csrf());

        when(service.hashUserPassword(any()))
                .thenReturn(userPatchEntity);
        when(service.updateUserById(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(user));
        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(userResponseDTO));

        verify(service).hashUserPassword(userPatchEntity);
        verify(service).updateUserById(userId, userPatchEntity);
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void updateInvalidUserByIdTest() throws Exception {
        // GIVEN
        long userId = 1L;
        UserDTO userPatch = new UserDTO(null, "invalidemail@", null);
        String invalidUserPatchData = mapper.writeValueAsString(userPatch);

        String adminEndpoint = USER_ENDPOINT + '/' + userId;
        RequestBuilder request = patch(adminEndpoint)
                .content(invalidUserPatchData)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf());
        // WHEN
        mvc.perform(request)
                // THEN
                .andExpect(status().isBadRequest());

        verify(service, never()).updateUserById(any(), any());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void updateUnknownUserByIdTest() throws Exception {
        // GIVEN
        UserDTO userPatch = new UserDTO("New Name", null, null);
        String userPatchData = mapper.writeValueAsString(userPatch);
        User userPatchEntity = new User(null, "New Name", null, null, null);

        long userId = 1L;
        String adminEndpoint = USER_ENDPOINT + '/' + userId;
        RequestBuilder request = patch(adminEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPatchData)
                .with(csrf());

        when(service.hashUserPassword(any()))
                .thenReturn(userPatchEntity);
        when(service.updateUserById(any(), any()))
                .thenThrow(EntityException.EntityNotFoundException.class);
        // WHEN
        mvc.perform(request)
                // THEN
                .andExpect(status().isNotFound());

        verify(service).hashUserPassword(userPatchEntity);
        verify(service).updateUserById(userId, userPatchEntity);
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void replaceUserByIdTest() throws Exception {
        // GIVEN
        String newUserData = mapper.writeValueAsString(userDTO);
        User expected = new User(user);
        expected.setId(null);

        long userId = 1L;
        String adminEndpoint = USER_ENDPOINT + '/' + userId;
        RequestBuilder request = put(adminEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(newUserData)
                .with(csrf());

        when(service.hashUserPassword(any()))
                .thenReturn(expected);
        when(service.replaceUserById(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(user));
        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(userResponseDTO));

        verify(service).hashUserPassword(expected);
        verify(service).replaceUserById(userId, expected);
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void replaceInvalidUserByIdTest() throws Exception {
        // GIVEN
        UserDTO newUser = new UserDTO(" ", "test@test.com", "mypassword");
        String newUserData = mapper.writeValueAsString(newUser);

        long userId = 1L;
        String adminEndpoint = USER_ENDPOINT + '/' + userId;
        RequestBuilder request = put(adminEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(newUserData)
                .with(csrf());

        when(service.replaceUserById(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(user));
        // WHEN
        mvc.perform(request)
                // THEN
                .andExpect(status().isBadRequest());

        verify(service, never()).replaceUserById(any(), any());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void deleteUserByIdTest() throws Exception {
        // GIVEN
        long userId = 1L;
        String adminEndpoint = USER_ENDPOINT + '/' + userId;
        RequestBuilder request = delete(adminEndpoint)
                .with(csrf());
        
        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isOk());

        verify(service).deleteUserById(userId);
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void deleteUnknownUserByIdTest() throws Exception {
        // GIVEN
        long userId = 1L;
        String adminEndpoint = USER_ENDPOINT + '/' + userId;
        RequestBuilder request = delete(adminEndpoint)
                .with(csrf());

        doThrow(EntityException.EntityNotFoundException.class)
                .when(service).deleteUserById(any());
        // WHEN
        mvc.perform(request)
                // THEN
                .andExpect(status().isNotFound());

        verify(service).deleteUserById(userId);
    }

    /*
    ALLOWED TO ANY USER
     */

    @Test
    void createUserTest() throws Exception {
        // GIVEN
        String userData = mapper.writeValueAsString(userDTO);
        User expected = new User(user);
        expected.setId(null);

        RequestBuilder request = post(USER_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userData)
                .with(csrf());

        when(service.hashUserPassword(any()))
                .thenReturn(expected);
        when(service.createUser(any())).thenReturn(CompletableFuture.completedFuture(user));
        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isCreated())
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(userResponseDTO));

        verify(service).hashUserPassword(expected);
        verify(service).createUser(expected);
    }

    @Test
    void createInvalidUserTest() throws Exception {
        // GIVEN
        UserDTO user = new UserDTO(null, "myemail@test.com", "MyPassword");
        String userData = mapper.writeValueAsString(user);

        RequestBuilder request = post(USER_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userData)
                .with(csrf());

        when(service.createUser(any())).thenReturn(CompletableFuture.completedFuture(this.user));
        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isBadRequest());

        verify(service, never()).createUser(any());
    }

    @Test
    void createAlreadyCreatedUserTest() throws Exception {
        // GIVEN
        String userData = mapper.writeValueAsString(userDTO);
        User expected = new User(user);
        expected.setId(null);

        RequestBuilder request = post(USER_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userData)
                .with(csrf());

        when(service.hashUserPassword(any()))
                .thenReturn(expected);
        when(service.createUser(any())).thenThrow(EntityException.class);
        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isBadRequest());

        verify(service).hashUserPassword(expected);
        verify(service).createUser(expected);
    }

    @Test
    void getCurrentUserTest() throws Exception {
        // GIVEN
        RequestBuilder request = get(USER_ENDPOINT);

        when(service.getUserById(any()))
                .thenReturn(CompletableFuture.completedFuture(user));
        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(userResponseDTO));

        verify(service).getUserById(Long.valueOf(USER_ID));
    }

    @Test
    void updateCurrentUserTest() throws Exception {
        // GIVEN
        UserDTO userUpdate = new UserDTO("NewName", null, null);
        String userUpdateData = mapper.writeValueAsString(userUpdate);

        User expected = new User(null, "NewName", null, null, null);

        Long currentUserId = Long.valueOf(USER_ID);

        RequestBuilder request = patch(USER_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userUpdateData)
                .with(csrf());

        when(service.hashUserPassword(any()))
                .thenReturn(expected);
        when(service.updateUserById(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(user));
        // WHEN
        mvc.perform(request)
                // THEN
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(userResponseDTO));

        verify(service).hashUserPassword(expected);
        verify(service).updateUserById(currentUserId, expected);
    }

    @Test
    void replaceCurrentUserTest() throws Exception {
        // GIVEN
        Long currentUserId = Long.valueOf(USER_ID);
        String newUserData = mapper.writeValueAsString(userDTO);
        User expected = new User(user);
        expected.setId(null);

        RequestBuilder request = put(USER_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(newUserData)
                .with(csrf());

        when(service.hashUserPassword(any()))
                .thenReturn(expected);
        when(service.replaceUserById(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(user));
        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(userResponseDTO));

        verify(service).hashUserPassword(expected);
        verify(service).replaceUserById(currentUserId, expected);
    }

    @Test
    void deleteCurrentUserTest() throws Exception {
        // GIVEN
        Long currentUserId = Long.valueOf(USER_ID);

        RequestBuilder request = delete(USER_ENDPOINT)
                .with(csrf());
        // WHEN
        mvc.perform(request);
        
        // THEN
        verify(service).deleteUserById(currentUserId);
    }


}
