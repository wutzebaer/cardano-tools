package de.peterspace.cardanominter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CardanoMinterApplication {

	public static void main(String[] args) {
		SpringApplication.run(CardanoMinterApplication.class, args);
	}

}
