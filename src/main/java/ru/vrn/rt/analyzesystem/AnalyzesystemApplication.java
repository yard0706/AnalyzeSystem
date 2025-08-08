package ru.vrn.rt.analyzesystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AnalyzesystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnalyzesystemApplication.class, args);
	}

}
