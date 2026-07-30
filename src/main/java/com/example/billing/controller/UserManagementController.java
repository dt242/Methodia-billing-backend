package com.example.billing.controller;

import com.example.billing.dto.UserCreateDto;
import com.example.billing.dto.UserUpdateDto;
import com.example.billing.model.Role;
import com.example.billing.model.User;
import com.example.billing.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*")
public class UserManagementController {

    private final UserService userService;

    public UserManagementController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean active) {

        return ResponseEntity.ok(userService.getUsers(username, role, active));
    }

    @PostMapping
    public ResponseEntity<String> createUser(@Valid @RequestBody UserCreateDto dto) {
        userService.createUser(dto);
        return ResponseEntity.ok("Потребителят е създаден успешно.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable String id, @Valid @RequestBody UserUpdateDto dto) {
        userService.updateUser(id, dto);
        return ResponseEntity.ok("Данните на потребителя са обновени.");
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivateUser(@PathVariable String id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok("Потребителят е деактивиран.");
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<String> activateUser(@PathVariable String id) {
        userService.activateUser(id);
        return ResponseEntity.ok("Потребителят е активиран отново.");
    }
}