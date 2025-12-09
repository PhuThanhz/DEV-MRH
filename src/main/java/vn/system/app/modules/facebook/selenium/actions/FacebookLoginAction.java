package vn.system.app.modules.facebook.selenium.actions;

import vn.system.app.modules.facebook.domain.Account;
import vn.system.app.modules.facebook.selenium.utils.CookieLoader;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FacebookLoginAction {

    @Autowired
    private CookieLoader cookieLoader;

    /**
     * Đăng nhập Facebook bằng cookie đã lưu
     */
    public void login(WebDriver driver, Account account) throws Exception {
        driver.get("https://facebook.com");
        Thread.sleep(1500);
        cookieLoader.loadCookies(driver, account.getCookieJson());
        driver.navigate().refresh();
        Thread.sleep(3000);
    }

    /**
     * Chỉ login từ file cookies.json trên ổ đĩa
     */
    public void loginFromFile(WebDriver driver, String cookieFilePath) throws Exception {
        driver.get("https://facebook.com");
        Thread.sleep(1500);
        cookieLoader.loadCookiesFromFile(driver, cookieFilePath);
        driver.navigate().refresh();
        Thread.sleep(3000);
    }
}
