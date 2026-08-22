package com.quoraBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

@SpringBootApplication
@EnableReactiveMongoAuditing
public class QuoraBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuoraBackendApplication.class, args);
	}

}
