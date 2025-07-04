package com.bob.smash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SmashApplication {
	public static void main(String[] args) {
		SpringApplication.run(SmashApplication.class, args);
	}
}