package com.ecommerce.user_service.service;

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

}
