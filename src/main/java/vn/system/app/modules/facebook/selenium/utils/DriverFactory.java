package vn.system.app.modules.facebook.selenium.utils;

import vn.system.app.modules.facebook.domain.Account;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Tạo WebDriver (Chrome) cho từng tài khoản Facebook
 */
@Component
public class DriverFactory {

    /**
     * Tạo ChromeDriver cho 1 account cụ thể
     * 
     * @param acc tài khoản Facebook (có profilePath)
     * @return WebDriver instance
     */
    public WebDriver createDriver(Account acc) {
        ChromeOptions options = new ChromeOptions();

        // mỗi tài khoản có thư mục profile riêng
        if (acc.getProfilePath() != null && !acc.getProfilePath().isEmpty()) {
            options.addArguments("--user-data-dir=" + acc.getProfilePath());
        }

        options.addArguments("--disable-notifications");
        options.addArguments("--start-maximized");
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));

        // tạo ChromeDriver mới
        WebDriver driver = new ChromeDriver(options);
        return driver;
    }
}
