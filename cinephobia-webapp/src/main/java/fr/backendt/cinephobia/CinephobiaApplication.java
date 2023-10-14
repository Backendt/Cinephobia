package fr.backendt.cinephobia;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class CinephobiaApplication {

	public static void main(String[] args) {
		SpringApplication.run(CinephobiaApplication.class, args);
	}

	@Bean
	public LayoutDialect layoutDialect() { // Enable Thymeleaf layout
		return new LayoutDialect();
	}

}
