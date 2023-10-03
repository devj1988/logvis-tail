package com.devj1988.lola;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LolaApplication {

	public static void main(String[] args) {
		SpringApplication.run(LolaApplication.class, args);
	}

}
