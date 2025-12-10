package vn.system.app.modules.facebook.selenium.utils;

import vn.system.app.modules.facebook.domain.Account;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;
import java.util.Collections;

@Component
public class DriverFactory {

    public WebDriver createDriver(Account acc) {
        ChromeOptions options = new ChromeOptions();

        // mỗi tài khoản có profile riêng
        if (acc.getProfilePath() != null && !acc.getProfilePath().isEmpty()) {
            options.addArguments("--user-data-dir=" + acc.getProfilePath());
        }

        options.addArguments("--disable-notifications");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-extensions");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--start-maximized");
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));

        return new ChromeDriver(options);
    }
}
