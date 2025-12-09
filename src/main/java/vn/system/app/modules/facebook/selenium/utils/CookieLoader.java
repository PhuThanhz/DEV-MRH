package vn.system.app.modules.facebook.selenium.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Map;

@Component
public class CookieLoader {

    private final ObjectMapper mapper = new ObjectMapper();

    public void loadCookies(WebDriver driver, String cookieJson) throws Exception {
        List<Map<String, Object>> cookies = mapper.readValue(cookieJson, List.class);
        addCookies(driver, cookies);
    }

    public void loadCookiesFromFile(WebDriver driver, String filePath) throws Exception {
        List<Map<String, Object>> cookies = mapper.readValue(new File(filePath), List.class);
        addCookies(driver, cookies);
    }

    private void addCookies(WebDriver driver, List<Map<String, Object>> cookies) {
        for (Map<String, Object> c : cookies) {
            Cookie cookie = new Cookie.Builder(
                    (String) c.get("name"),
                    (String) c.get("value"))
                    .domain((String) c.get("domain"))
                    .path((String) c.get("path"))
                    .isSecure((Boolean) c.get("secure"))
                    .build();
            driver.manage().addCookie(cookie);
        }
    }
}
