package fr.backendt.cinephobia.controllers;

import fr.backendt.cinephobia.exceptions.BadRequestException;
import fr.backendt.cinephobia.models.User;
import fr.backendt.cinephobia.models.dto.UserDTO;
import fr.backendt.cinephobia.services.UserService;
import fr.backendt.cinephobia.utils.UrlEncodedFormSerializer;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser
@WebMvcTest(AuthenticationController.class)
class AuthenticationControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService service;

    private UserDTO userDto;
    private User user;

    @BeforeEach
    void initTestValues() {
        userDto = new UserDTO("User name", "my.user@email.com", "Password1234");
        user = new User("User name", "my.user@email.com", "Password1234", null);
    }

    @Test
    void getRegistrationPageTest() throws Exception {
        // GIVEN
        UserDTO expected = new UserDTO(null, null, null);
        RequestBuilder request = get("/register");

        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isOk())
                .andExpect(model().attribute("user", expected));
    }

    @Test
    void registerUserTest() throws Exception {
        // GIVEN
        String userData = UrlEncodedFormSerializer.serialize(userDto);

        RequestBuilder request = post("/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(userData)
                .with(csrf());

        MvcResult result;

        when(service.createUser(any()))
                .thenReturn(CompletableFuture.completedFuture(user));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"))
                .andExpect(model().hasNoErrors());

        verify(service).createUser(user);
    }

    @CsvSource(ignoreLeadingAndTrailingWhitespace = false,
            value = {
                    "     ,user@test.com,MyPassword1234",
                    ",user@test.com,MyPassword1234",
                    "a,user@test.com,MyPassword1234",
                    "username,notanemail,MyPassword1234",
                    "username,     ,MyPassword1234",
                    "username,,MyPassword1234",
                    "username,user@test.com,aaa",
                    "username,user@test.com,"
            }
    )
    @ParameterizedTest
    void registerInvalidUserTest(String name, String email, String password) throws Exception {
        // GIVEN
        UserDTO invalidUserDTO = new UserDTO(name, email, password);
        String userData = UrlEncodedFormSerializer.serialize(invalidUserDTO);

        RequestBuilder request = post("/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(userData)
                .with(csrf());

        MvcResult result;

        when(service.hashUserPassword(any()))
                .thenReturn(user);
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors());

        verify(service, never()).hashUserPassword(any());
        verify(service, never()).createUser(any());
    }

    @Test
    void registerExistentUserTest() throws Exception {
        // GIVEN
        String userData = UrlEncodedFormSerializer.serialize(userDto);

        RequestBuilder request = post("/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(userData)
                .with(csrf());

        MvcResult result;

        when(service.createUser(any()))
                .thenReturn(CompletableFuture.failedFuture(
                        new BadRequestException("Error")
                ));
        // WHEN
        result = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        // THEN
        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("user", "email", "email-taken"));

        verify(service).createUser(user);
    }

    @Test
    void getLoginPageTest() throws Exception {
        // GIVEN
        RequestBuilder request = get("/login");

        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isOk());
    }

}
