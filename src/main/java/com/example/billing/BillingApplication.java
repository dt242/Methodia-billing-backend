package com.example.billing;

import com.example.billing.service.CsvParserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BillingApplication {

	public static void main(String[] args) {
		SpringApplication.run(BillingApplication.class, args);
	}
	@Bean
	CommandLineRunner testRunner(CsvParserService csvParserService) {
		return args -> {
			csvParserService.importUsers();
			csvParserService.importPrices(1);
			csvParserService.importPrices(2);
			csvParserService.importReadings();
		};
	}

}
