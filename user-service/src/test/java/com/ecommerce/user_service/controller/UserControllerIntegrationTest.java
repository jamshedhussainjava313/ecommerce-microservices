package com.ecommerce.user_service.controller;

import com.ecommerce.user_service.entity.User;
import com.ecommerce.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("ci")
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {

        String requestBody = """
                {
                    "name": "Jamshed",
                    "email": "integration@example.com",
                    "password": "Password123"
                }
                """;

        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Jamshed"))
                .andExpect(jsonPath("$.email")
                        .value("integration@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldRejectInvalidRequest() throws Exception {

        String requestBody = """
                {
                    "name": "",
                    "email": "invalid-email",
                    "password": "123"
                }
                """;

        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {

        User existingUser = new User();
        existingUser.setName("Existing User");
        existingUser.setEmail("duplicate@example.com");
        existingUser.setPassword("hashedPassword");

        userRepository.save(existingUser);

        String requestBody = """
            {
                "name": "Jamshed",
                "email": "duplicate@example.com",
                "password": "Password123"
            }
            """;

        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Email already registered"));
    }

}
