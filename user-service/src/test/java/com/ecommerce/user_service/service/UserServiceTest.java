package com.ecommerce.user_service.service;

import com.ecommerce.user_service.dto.UpdateUserRequest;
import com.ecommerce.user_service.entity.User;
import com.ecommerce.user_service.exception.UserAlreadyExistsException;
import com.ecommerce.user_service.exception.UserNotFoundException;
import com.ecommerce.user_service.repository.UserRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldRegisterUserSuccessfully() {

        User user = new User();
        user.setName("Jamshed");
        user.setEmail("jamshed@example.com");
        user.setPassword("Password123");

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("Password123"))
                .thenReturn("hashedPassword");

        when(userRepository.save(user))
                .thenReturn(user);

        User result = userService.registerUser(user);

        assertNotNull(result);
        assertEquals("jamshed@example.com", result.getEmail());
        assertEquals("hashedPassword", result.getPassword());

        verify(userRepository).findByEmail("jamshed@example.com");
        verify(passwordEncoder).encode("Password123");
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {

        User existingUser = new User();
        existingUser.setEmail("jamshed@example.com");

        User user = new User();
        user.setName("Jamshed");
        user.setEmail("jamshed@example.com");
        user.setPassword("Password123");

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(existingUser));

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.registerUser(user)
        );

        verify(userRepository).findByEmail("jamshed@example.com");

        verify(userRepository, never()).save(any(User.class));

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void shouldGetUserByIdSuccessfully() {

        User user = new User();
        user.setId(67L);
        user.setName("Docker User");
        user.setEmail("dockeruser@example.com");
        user.setPassword("hashed-password");

        when(userRepository.findById(67L))
                .thenReturn(Optional.of(user));

        User result = userService.getUserById(67L);

        assertEquals(67L, result.getId());
        assertEquals("Docker User", result.getName());
        assertEquals("dockeruser@example.com", result.getEmail());

        verify(userRepository).findById(67L);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        when(userRepository.findById(9999L))
                .thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserById(9999L)
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findById(9999L);
    }

    @Test
    void shouldUpdateUserSuccessfully() {

        User existingUser = new User();
        existingUser.setId(67L);
        existingUser.setName("Old Name");
        existingUser.setEmail("old@example.com");
        existingUser.setPassword("old-hashed-password");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");
        request.setEmail("updated@example.com");
        request.setPassword("newpassword123");

        when(userRepository.findById(67L))
                .thenReturn(Optional.of(existingUser));

        when(userRepository.findByEmail("updated@example.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("newpassword123"))
                .thenReturn("new-hashed-password");

        when(userRepository.save(existingUser))
                .thenReturn(existingUser);

        User result = userService.updateUser(67L, request);

        assertEquals("Updated Name", result.getName());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("new-hashed-password", result.getPassword());

        verify(userRepository).findById(67L);
        verify(userRepository).findByEmail("updated@example.com");
        verify(passwordEncoder).encode("newpassword123");
        verify(userRepository).save(existingUser);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingUser() {

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");
        request.setEmail("updated@example.com");
        request.setPassword("newpassword123");

        when(userRepository.findById(999999L))
                .thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUser(999999L, request)
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findById(999999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailBelongsToAnotherUser() {

        User existingUser = new User();
        existingUser.setId(67L);

        User anotherUser = new User();
        anotherUser.setId(68L);
        anotherUser.setEmail("another@example.com");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");
        request.setEmail("another@example.com");
        request.setPassword("newpassword123");

        when(userRepository.findById(67L))
                .thenReturn(Optional.of(existingUser));

        when(userRepository.findByEmail("another@example.com"))
                .thenReturn(Optional.of(anotherUser));

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.updateUser(67L, request)
        );

        assertEquals("Email already registered", exception.getMessage());

        verify(userRepository).findById(67L);
        verify(userRepository).findByEmail("another@example.com");
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void shouldAllowUserToKeepOwnEmail() {

        User existingUser = new User();
        existingUser.setId(67L);
        existingUser.setName("Old Name");
        existingUser.setEmail("same@example.com");
        existingUser.setPassword("old-hashed-password");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");
        request.setEmail("same@example.com");
        request.setPassword("newpassword123");

        when(userRepository.findById(67L))
                .thenReturn(Optional.of(existingUser));

        when(userRepository.findByEmail("same@example.com"))
                .thenReturn(Optional.of(existingUser));

        when(passwordEncoder.encode("newpassword123"))
                .thenReturn("new-hashed-password");

        when(userRepository.save(existingUser))
                .thenReturn(existingUser);

        User result = userService.updateUser(67L, request);

        assertEquals("Updated Name", result.getName());
        assertEquals("same@example.com", result.getEmail());
        assertEquals("new-hashed-password", result.getPassword());

        verify(userRepository).save(existingUser);
    }

}
