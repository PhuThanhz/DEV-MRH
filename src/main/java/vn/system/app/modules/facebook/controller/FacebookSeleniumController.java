package vn.system.app.modules.facebook.controller;

import vn.system.app.modules.facebook.domain.Account;
import vn.system.app.modules.facebook.selenium.actions.FacebookLoginAction;
import vn.system.app.modules.facebook.selenium.actions.FacebookPostAction;
import vn.system.app.modules.facebook.selenium.utils.DriverFactory;

import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/v1/facebook")
public class FacebookSeleniumController {

    @Autowired
    private FacebookLoginAction loginAction;

    @Autowired
    private FacebookPostAction postAction;

    @Autowired
    private DriverFactory driverFactory;

    private WebDriver driver;

    @PostMapping("/post-to-group")
    public String postToGroup(@RequestParam("content") String content) {
        String groupUrl = "https://www.facebook.com/groups/184659109462650";

        try {
            // 1️ Đọc cookie JSON
            String cookiePath = "src/main/java/vn/system/app/modules/facebook/controller/cookies2.json";
            String cookieJson = new String(Files.readAllBytes(Paths.get(cookiePath)));

            // 2️ Tạo account tạm
            Account acc = new Account();
            acc.setCookieJson(cookieJson);
            acc.setProfilePath("C:\\selenium_profile_test");

            // 3️ Tạo WebDriver
            driver = driverFactory.createDriver(acc);

            // 4️ Login Facebook
            loginAction.login(driver, acc);
            System.out.println(" Login thành công.");

            // 5️ Mở group
            postAction.openTarget(driver, groupUrl);
            System.out.println(" Đang mở group: " + groupUrl);

            // 6️ Mở form “Bạn viết gì đi...”
            postAction.openCreatePostBox(driver);
            System.out.println(" Đang mở form viết bài...");

            // 7️ Nhập nội dung bài viết
            postAction.typeContent(driver, content);
            System.out.println(" Nhập nội dung xong.");

            // 8️ Nhấn nút “Đăng”
            postAction.clickPostButton(driver);
            System.out.println(" Đã click nút Đăng.");

            // 9️ Lấy link bài đăng
            String link = postAction.getPostLink(driver);
            System.out.println(" Bài đăng hoàn tất. Link: " + link);

            return " Đăng bài thành công! Link bài viết: " + link;

        } catch (Exception e) {
            e.printStackTrace();
            return " Lỗi khi đăng bài: " + e.getMessage();
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                    driver = null;
                    System.out.println(" ChromeDriver closed.");
                } catch (Exception ignore) {
                }
            }
        }
    }
}
