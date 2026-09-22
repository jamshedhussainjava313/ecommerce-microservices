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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("ci")
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    @Test
    void shouldGetUserByIdSuccessfully() throws Exception {

        User user = new User();
        user.setName("Get User Test");
        user.setEmail("getuser@example.com");
        user.setPassword("hashed-password");

        User savedUser = userRepository.save(user);

        mockMvc.perform(get("/users/" + savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.name").value("Get User Test"))
                .andExpect(jsonPath("$.email").value("getuser@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {

        mockMvc.perform(get("/users/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void shouldUpdateUserSuccessfully() throws Exception {

        User user = new User();
        user.setName("Old Name");
        user.setEmail("update@example.com");
        user.setPassword("old-hashed-password");

        User savedUser = userRepository.save(user);

        mockMvc.perform(put("/users/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Updated Name",
                                "email": "updated@example.com",
                                "password": "newpassword123"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingUser() throws Exception {

        mockMvc.perform(put("/users/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Updated Name",
                                "email": "updated@example.com",
                                "password": "newpassword123"
                            }
                            """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void shouldReturn409WhenUpdatingWithExistingEmail() throws Exception {

        User user1 = new User();
        user1.setName("User One");
        user1.setEmail("user1@example.com");
        user1.setPassword("hashed-password");

        User user2 = new User();
        user2.setName("User Two");
        user2.setEmail("user2@example.com");
        user2.setPassword("hashed-password");

        User savedUser1 = userRepository.save(user1);
        userRepository.save(user2);

        mockMvc.perform(put("/users/" + savedUser1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Updated User",
                                "email": "user2@example.com",
                                "password": "newpassword123"
                            }
                            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already registered"));
    }

    @Test
    void shouldRejectInvalidUpdateRequest() throws Exception {

        User user = new User();
        user.setName("Valid User");
        user.setEmail("valid@example.com");
        user.setPassword("hashed-password");

        User savedUser = userRepository.save(user);

        mockMvc.perform(put("/users/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "",
                                "email": "invalid-email",
                                "password": "123"
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHashPasswordWhenUpdatingUser() throws Exception {

        User user = new User();
        user.setName("Password User");
        user.setEmail("password-update@example.com");
        user.setPassword("old-hashed-password");

        User savedUser = userRepository.save(user);

        mockMvc.perform(put("/users/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Password User",
                                "email": "password-update@example.com",
                                "password": "newpassword123"
                            }
                            """))
                .andExpect(status().isOk());

        User updatedUser = userRepository.findById(savedUser.getId())
                .orElseThrow();

        assertNotEquals("newpassword123", updatedUser.getPassword());
        assertTrue(passwordEncoder.matches(
                "newpassword123",
                updatedUser.getPassword()
        ));
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        User user1 = new User();
        user1.setName("User One");
        user1.setEmail("user1@example.com");
        user1.setPassword("hashed-password-1");

        User user2 = new User();
        user2.setName("User Two");
        user2.setEmail("user2@example.com");
        user2.setPassword("hashed-password-2");

        userRepository.save(user1);
        userRepository.save(user2);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("User One"))
                .andExpect(jsonPath("$[0].email").value("user1@example.com"))
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[1].name").value("User Two"))
                .andExpect(jsonPath("$[1].email").value("user2@example.com"))
                .andExpect(jsonPath("$[1].password").doesNotExist());
    }
    @Test
    void shouldReturnEmptyListWhenNoUsersExist() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

}
