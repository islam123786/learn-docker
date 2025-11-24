package com.islam.simple_spring.boot_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@SpringBootApplication
@RestController
public class SimpleSpringBootAppApplication {

	@GetMapping("/greeting")
	public String hello(){
		return "Hi Islam, This is a simple Springboot application";
	}

	public static void main(String[] args) {
		SpringApplication.run(SimpleSpringBootAppApplication.class, args);
	}

}
