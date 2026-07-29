package com.example.billing.repository;

import com.example.billing.model.Role;
import com.example.billing.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByReference(String reference);
    long countByRole(Role role);
    @Query("SELECT u FROM User u WHERE " +
            "(:username IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :username, '%')) OR LOWER(u.reference) LIKE LOWER(CONCAT('%', :username, '%'))) AND " +
            "(:role IS NULL OR u.role = :role) AND " +
            "(:active IS NULL OR u.active = :active)")
    List<User> findWithFilters(@Param("username") String username,
                               @Param("role") Role role,
                               @Param("active") Boolean active);
}