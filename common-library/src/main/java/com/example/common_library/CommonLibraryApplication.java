package com.example.common_library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication  // Annotation Spring qui marque cette classe comme point d'entrée principal de l'application Spring Boot.
public class CommonLibraryApplication {
	public static void main(String[] args) {
		// Lance l'application Spring Boot
		SpringApplication.run(CommonLibraryApplication.class, args);
	}
}
