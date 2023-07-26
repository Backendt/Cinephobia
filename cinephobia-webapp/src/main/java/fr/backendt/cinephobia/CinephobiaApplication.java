package fr.backendt.cinephobia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class CinephobiaApplication {

	public static void main(String[] args) {
		SpringApplication.run(CinephobiaApplication.class, args);
	}

}
