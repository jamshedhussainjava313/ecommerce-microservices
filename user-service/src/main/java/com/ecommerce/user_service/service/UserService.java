package com.ecommerce.user_service.service;

import com.ecommerce.user_service.dto.UpdateUserRequest;
import com.ecommerce.user_service.entity.User;
import com.ecommerce.user_service.exception.UserAlreadyExistsException;
import com.ecommerce.user_service.exception.UserNotFoundException;
import com.ecommerce.user_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,PasswordEncoder passwordEncoder){
        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;
    }


    public User registerUser(User user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already registered");
        }
        String hashPassword=passwordEncoder.encode(user.getPassword());
        user.setPassword(hashPassword);
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public User updateUser(Long id, UpdateUserRequest request) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        userRepository.findByEmail(request.getEmail())
                .ifPresent(user -> {
                    if (!user.getId().equals(id)) {
                        throw new UserAlreadyExistsException("Email already registered");
                    }
                });

        existingUser.setName(request.getName());
        existingUser.setEmail(request.getEmail());

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        existingUser.setPassword(hashedPassword);

        return userRepository.save(existingUser);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

}
