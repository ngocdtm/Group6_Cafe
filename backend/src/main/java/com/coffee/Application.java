package com.coffee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.coffee.repository")
@EntityScan(basePackages = "com.coffee.entity")
@CrossOrigin(origins = "http://localhost:4200")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
