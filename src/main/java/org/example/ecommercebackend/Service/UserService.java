package org.example.ecommercebackend.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ecommercebackend.DTO.RequestDTO.UserRequestDTO;
import org.example.ecommercebackend.DTO.ResponseDTO.UserResponseDTO;
import org.example.ecommercebackend.Entity.User;
import org.example.ecommercebackend.Exception.DuplicateEmailException;
import org.example.ecommercebackend.Exception.ResourceNotFoundException;
import org.example.ecommercebackend.Repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserResponseDTO registerUser(UserRequestDTO userRequestDTO) {
        log.info("Registering new user with email={}", userRequestDTO.getEmail());

        if (userRepository.existsByEmail(userRequestDTO.getEmail())) {
            log.warn("Registration failed: email already exists - {}", userRequestDTO.getEmail());
            throw new DuplicateEmailException("Email already exists: " + userRequestDTO.getEmail());
        }

        User user = modelMapper.map(userRequestDTO, User.class);
        user.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));
        user.setRole(User.Role.CUSTOMER);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with id={}, email={}", savedUser.getId(), savedUser.getEmail());

        try {
            emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFirstname());
            log.debug("Welcome email sent to {}", savedUser.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}", savedUser.getEmail(), e);
        }

        return modelMapper.map(savedUser, UserResponseDTO.class);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Integer userId) {
        log.debug("Fetching user by id={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with id={}", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });
        return modelMapper.map(user, UserResponseDTO.class);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserByEmail(String email) {
        log.debug("Fetching user by email={}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email={}", email);
                    return new ResourceNotFoundException("User not found with email: " + email);
                });
        return modelMapper.map(user, UserResponseDTO.class);
    }

    public UserResponseDTO updateUser(Integer userId, UserRequestDTO userRequestDTO) {
        log.info("Updating userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Cannot update: user not found with id={}", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });

        if (!user.getEmail().equals(userRequestDTO.getEmail()) &&
                userRepository.existsByEmail(userRequestDTO.getEmail())) {
            log.warn("Update failed for userId={}: email already exists - {}", userId, userRequestDTO.getEmail());
            throw new DuplicateEmailException("Email already exists: " + userRequestDTO.getEmail());
        }

        user.setFirstname(userRequestDTO.getFirstname());
        user.setLastname(userRequestDTO.getLastname());
        user.setEmail(userRequestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));

        User updatedUser = userRepository.save(user);
        log.info("User id={} updated successfully", userId);

        return modelMapper.map(updatedUser, UserResponseDTO.class);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        log.debug("Fetching all users");
        List<UserResponseDTO> users = userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, UserResponseDTO.class))
                .collect(Collectors.toList());
        log.debug("Retrieved {} user(s)", users.size());
        return users;
    }

    public void deleteUser(Integer userId) {
        log.info("Deleting userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Cannot delete: user not found with id={}", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });
        userRepository.delete(user);
        log.info("User id={} deleted successfully", userId);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public User getUserEntityById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User entity not found with id={}", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });
    }
}