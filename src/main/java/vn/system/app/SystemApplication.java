package vn.system.app;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SystemApplication {

	public static void main(String[] args) {
		// Tự động tải và cấu hình ChromeDriver (Selenium)
		WebDriverManager.chromedriver().setup();

		// Chạy ứng dụng Spring Boot
		SpringApplication.run(SystemApplication.class, args);

	}
}
