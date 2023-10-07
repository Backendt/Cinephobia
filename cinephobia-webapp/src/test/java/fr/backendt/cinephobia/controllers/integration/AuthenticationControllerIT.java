package fr.backendt.cinephobia.controllers.integration;

import fr.backendt.cinephobia.models.dto.UserDTO;
import fr.backendt.cinephobia.utils.UrlEncodedFormSerializer;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthenticationControllerIT {

    @Autowired
    private MockMvc mvc;

    @Test
    void registerUserTest() throws Exception {
        // GIVEN
        UserDTO userDTO = new UserDTO("My User", "user@test.com", "MyPassword1234");
        String userData = UrlEncodedFormSerializer.serialize(userDTO);

        RequestBuilder request = post("/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(userData)
                .with(csrf());

        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"))
                .andExpect(model().hasNoErrors());
    }

    @Test
    void registerDuplicateUserTest() throws Exception {
        // GIVEN
        UserDTO userDTO = new UserDTO("Duplicate User", "john.doe@test.com", "MyPassword1234");
        String userData = UrlEncodedFormSerializer.serialize(userDTO);

        RequestBuilder request = post("/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(userData)
                .with(csrf());

        // WHEN
        mvc.perform(request)
        // THEN
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("user", "email", "email-taken"));
    }

}
