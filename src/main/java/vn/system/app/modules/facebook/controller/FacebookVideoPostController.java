package vn.system.app.modules.facebook.controller;

import vn.system.app.modules.facebook.domain.Account;
import vn.system.app.modules.facebook.selenium.actions.FacebookLoginAction;
import vn.system.app.modules.facebook.selenium.actions.FacebookPostAction;
import vn.system.app.modules.facebook.selenium.utils.DriverFactory;
import vn.system.app.modules.sourcelink.domain.SourceLink;
import vn.system.app.modules.sourcelink.service.SourceLinkService;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

@RestController
@RequestMapping("/api/v1/facebook")
public class FacebookVideoPostController {

    @Autowired
    private FacebookLoginAction loginAction;

    @Autowired
    private FacebookPostAction postAction;

    @Autowired
    private DriverFactory driverFactory;

    @Autowired
    private SourceLinkService linkService;

    private WebDriver driver;

    private static final String GROUP_URL = "https://www.facebook.com/groups/184659109462650";

    @PostMapping("/post-from-link/{linkId}")
    public String postFromLink(@PathVariable("linkId") Long linkId) {
        try {
            // 1️⃣ Lấy dữ liệu từ DB
            SourceLink link = linkService.findById(linkId);
            String caption = (link.getCaption() != null && !link.getCaption().isBlank())
                    ? link.getCaption()
                    : "(Không có caption)";
            System.out.println("📝 Caption từ DB: " + caption);

            // 2️⃣ Đọc cookie đăng nhập
            String cookiePath = "src/main/java/vn/system/app/modules/facebook/controller/cookies2.json";
            String cookieJson = new String(Files.readAllBytes(Paths.get(cookiePath)));

            // 3️⃣ Chuẩn bị WebDriver
            Account acc = new Account();
            acc.setCookieJson(cookieJson);
            acc.setProfilePath("C:\\selenium_profile_test");

            driver = driverFactory.createDriver(acc);
            loginAction.login(driver, acc);
            System.out.println("✅ Đăng nhập thành công.");

            // 4️⃣ Mở group
            driver.get(GROUP_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            Thread.sleep(4000);

            // 5️⃣ Mở form “Tạo bài viết”
            postAction.openCreatePostBox(driver);
            System.out.println("🪶 Form tạo bài viết đã mở.");

            // 6️⃣ Nhập caption qua JavaScript (hỗ trợ emoji)
            WebElement inputBox = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//div[@role='textbox' and @contenteditable='true']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].innerText = arguments[1];", inputBox, caption);
            System.out.println("📝 Nhập caption thành công.");

            // 7️⃣ Chờ nút “Đăng” xuất hiện và bật
            WebElement postButton = null;
            try {
                // Thử 3 dạng phổ biến nhất
                postButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
                        "//div[@role='button' and (contains(@aria-label,'Đăng') or contains(@aria-label,'Post')) and not(@aria-disabled='true')]")));
            } catch (Exception e1) {
                try {
                    postButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
                            "//span[normalize-space()='Đăng' or normalize-space()='Post']/ancestor::div[@role='button' and not(@aria-disabled='true')]")));
                } catch (Exception e2) {
                    postButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
                            "//div[@role='button' and not(@aria-disabled='true') and descendant::span[contains(text(),'Đăng') or contains(text(),'Post')]]")));
                }
            }

            // 8️⃣ Click “Đăng” qua JavaScript
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", postButton);
            Thread.sleep(800);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", postButton);
            System.out.println("📤 Đã click nút Đăng.");

            // 9️⃣ Chờ đăng xong
            Thread.sleep(15000);

            // 🔟 Lấy URL bài viết sau khi đăng
            String postUrl = driver.getCurrentUrl();
            System.out.println("✅ Đăng bài hoàn tất! Link: " + postUrl);

            return "✅ Đăng bài thành công từ link ID=" + linkId + " | " + postUrl;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Lỗi khi đăng bài từ link ID=" + linkId + ": " + e.getMessage());
            return "❌ Lỗi khi đăng bài từ link ID=" + linkId + ": " + e.getMessage();
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                    System.out.println("🧹 ChromeDriver closed.");
                } catch (Exception ignore) {
                }
            }
        }
    }
}
