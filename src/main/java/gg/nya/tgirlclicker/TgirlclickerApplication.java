package gg.nya.tgirlclicker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TgirlclickerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TgirlclickerApplication.class, args);
	}

}
