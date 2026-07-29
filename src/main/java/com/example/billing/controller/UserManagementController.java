package com.example.billing.controller;

import com.example.billing.dto.UserCreateDto;
import com.example.billing.dto.UserUpdateDto;
import com.example.billing.exception.InvalidDataException;
import com.example.billing.exception.ResourceNotFoundException;
import com.example.billing.model.Role;
import com.example.billing.model.User;
import com.example.billing.repository.UserRepository;
import com.example.billing.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*")
public class UserManagementController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserManagementController(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean active) {

        List<User> users = userRepository.findWithFilters(username, role, active);
        return ResponseEntity.ok(users);
    }


    @PostMapping
    public ResponseEntity<String> createUser(@Valid @RequestBody UserCreateDto dto) {
        if (userRepository.findByReference(dto.reference()).isPresent()) {
            throw new InvalidDataException("Потребител с ID " + dto.reference() + " вече съществува.");
        }

        String encodedPassword = passwordEncoder.encode(dto.password());
        User user = new User(dto.name(), dto.reference(), dto.tariffCode(), encodedPassword, dto.role());

        userRepository.save(user);
        auditService.logAction("User Management", "Created user with reference: " + dto.reference());

        return ResponseEntity.ok("Потребителят е създаден успешно.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable String id, @Valid @RequestBody UserUpdateDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Потребителят не е намерен."));

        user.setName(dto.name());
        user.setTariffCode(dto.tariffCode());
        user.setRole(dto.role());

        userRepository.save(user);
        auditService.logAction("User Management", "Updated user with reference: " + user.getReference());

        return ResponseEntity.ok("Данните на потребителя са обновени.");
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivateUser(@PathVariable String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Потребителят не е намерен."));

        user.setActive(false);
        userRepository.save(user);
        auditService.logAction("User Management", "Deactivated user with reference: " + user.getReference());

        return ResponseEntity.ok("Потребителят е деактивиран.");
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<String> activateUser(@PathVariable String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Потребителят не е намерен."));

        user.setActive(true);
        userRepository.save(user);
        auditService.logAction("User Management", "Activated user with reference: " + user.getReference());

        return ResponseEntity.ok("Потребителят е активиран отново.");
    }
}