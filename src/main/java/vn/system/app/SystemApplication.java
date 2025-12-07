package vn.system.app;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SystemApplication {
	public static void main(String[] args) {
		WebDriverManager.chromedriver().setup();
		SpringApplication.run(SystemApplication.class, args);
	}
}
