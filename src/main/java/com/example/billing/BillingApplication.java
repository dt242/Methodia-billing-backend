package com.example.billing;

import com.example.billing.model.Role;
import com.example.billing.model.User;
import com.example.billing.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class BillingApplication {

	public static void main(String[] args) {
		SpringApplication.run(BillingApplication.class, args);
	}

	@Bean
	CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (userRepository.findByReference("ADMIN-1").isEmpty()) {
				User admin = new User("System Admin", "ADMIN-1", 0,
						passwordEncoder.encode("admin123"), Role.ADMIN);
				userRepository.save(admin);
				System.out.println("=== Системният администратор е създаден! ===");
			}
		};
	}
}