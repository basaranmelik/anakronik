package com.badsector.anakronik;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AnakronikApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnakronikApplication.class, args);
	}

}
