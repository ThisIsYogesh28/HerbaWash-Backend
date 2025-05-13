package com.ByteShield.HerbaWash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HerbaWashApplication {

	public static void main(String[] args) {
		SpringApplication.run(HerbaWashApplication.class, args);
	}

}
