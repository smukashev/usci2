package kz.bsbnb.usci.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jandos Iskakov
 */

@SpringBootApplication
@Configuration
@EnableConfigurationProperties
public class EavApplication {

	public static void main(String[] args) {
		SpringApplication.run(EavApplication.class, args);
	}

}
