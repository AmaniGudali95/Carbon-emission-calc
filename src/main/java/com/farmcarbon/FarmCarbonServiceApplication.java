package com.farmcarbon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FarmCarbonServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FarmCarbonServiceApplication.class, args);
	}

}
