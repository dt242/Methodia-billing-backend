package com.example.billing.service;

import com.example.billing.dto.UserCreateDto;
import com.example.billing.dto.UserUpdateDto;
import com.example.billing.exception.InvalidDataException;
import com.example.billing.exception.ResourceNotFoundException;
import com.example.billing.model.Role;
import com.example.billing.model.User;
import com.example.billing.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    public List<User> getUsers(String username, Role role, Boolean active) {
        return userRepository.findWithFilters(username, role, active);
    }

    @Transactional
    public void createUser(UserCreateDto dto) {
        if (userRepository.findByReference(dto.reference()).isPresent()) {
            throw new InvalidDataException("Потребител с ID " + dto.reference() + " вече съществува.");
        }

        String encodedPassword = passwordEncoder.encode(dto.password());
        User user = new User(dto.name(), dto.reference(), dto.tariffCode(), encodedPassword, dto.role());

        userRepository.save(user);
        auditService.logAction("User Management", "Created user with reference: " + dto.reference());
    }

    @Transactional
    public void updateUser(String id, UserUpdateDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Потребителят не е намерен."));

        user.setName(dto.name());
        user.setTariffCode(dto.tariffCode());
        user.setRole(dto.role());

        userRepository.save(user);
        auditService.logAction("User Management", "Updated user with reference: " + user.getReference());
    }

    @Transactional
    public void deactivateUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Потребителят не е намерен."));

        user.setActive(false);
        userRepository.save(user);
        auditService.logAction("User Management", "Deactivated user with reference: " + user.getReference());
    }

    @Transactional
    public void activateUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Потребителят не е намерен."));

        user.setActive(true);
        userRepository.save(user);
        auditService.logAction("User Management", "Activated user with reference: " + user.getReference());
    }
}