package vn.system.app.modules.facebook.selenium.actions;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class FacebookPostAction {

    /**
     * Mở group hoặc page theo URL
     */
    public void openTarget(WebDriver driver, String url) throws InterruptedException {
        driver.get(url);
        Thread.sleep(5000); // đợi group load đầy đủ
    }

    /**
     * Click vào ô “Bạn viết gì đi...” để mở popup soạn bài
     */
    /**
     * Mở form “Bạn viết gì đi...” để bắt đầu đăng bài
     */
    public void openCreatePostBox(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            // ✅ 1️⃣ Tìm vùng chứa “Bạn viết gì đi...” (span hoặc div có text tương tự)
            WebElement createBox = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath(
                            "//span[contains(text(),'Bạn viết gì đi') or contains(text(),'Tạo bài viết') or contains(text(),'Write something')]")));

            // ✅ 2️⃣ Cuộn phần tử vào giữa màn hình (tránh bị menu đè)
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", createBox);
            Thread.sleep(1000);

            // ✅ 3️⃣ Click an toàn qua JavaScript (bỏ qua overlay)
            js.executeScript("arguments[0].click();", createBox);
            System.out.println("🪶 Click JavaScript mở khung tạo bài viết...");

            // ✅ 4️⃣ Đợi popup hiện hoàn toàn
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//div[@role='textbox' and @contenteditable='true']")));
            Thread.sleep(2000);

        } catch (Exception e1) {
            try {
                // fallback: click phần tử cha gần nhất chứa “Bạn viết gì đi...”
                WebElement fallbackBox = driver.findElement(
                        By.xpath("//span[contains(text(),'Bạn viết gì đi')]/ancestor::div[@role='button']"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});",
                        fallbackBox);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", fallbackBox);
                System.out.println("🪶 Click fallback mở form tạo bài viết...");
                Thread.sleep(2000);
            } catch (Exception e2) {
                throw new RuntimeException(
                        "❌ Không tìm thấy hoặc không click được vùng tạo bài viết: " + e2.getMessage());
            }
        }
    }

    /**
     * Nhập nội dung bài đăng vào ô nhập thực tế (contenteditable)
     */
    public void typeContent(WebDriver driver, String content) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            // ✅ Chờ phần tử textbox thật sự xuất hiện
            WebElement inputBox = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath(
                            "//div[@role='textbox' and @contenteditable='true' and contains(@aria-placeholder,'Tạo bài viết')]")));

            // ✅ Cuộn đến vùng hiển thị textbox và click bằng JS
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", inputBox);
            js.executeScript("arguments[0].focus();", inputBox);
            js.executeScript("arguments[0].click();", inputBox);
            Thread.sleep(800);

            // ✅ Gửi nội dung
            inputBox.sendKeys(content);
            Thread.sleep(1500);

            System.out.println("✍️ Nhập nội dung bài viết thành công.");

        } catch (Exception e) {
            throw new RuntimeException("❌ Không thể nhập nội dung bài viết: " + e.getMessage());
        }
    }

    /**
     * Click nút “Đăng” (hoạt động trên layout mới của Facebook)
     */
    public void clickPostButton(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            // ✅ Chờ nút Đăng sẵn sàng (bỏ aria-disabled="true")
            WebElement postBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[@role='button' and @aria-label='Đăng' and not(@aria-disabled='true')]")));

            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", postBtn);
            js.executeScript("arguments[0].click();", postBtn);
            System.out.println("📢 Click nút Đăng thành công.");

        } catch (Exception e1) {
            try {
                // fallback – tìm span có chữ “Đăng” bất kể aria-label
                WebElement altPostBtn = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath(
                                "//span[normalize-space()='Đăng']/ancestor::div[@role='button' and not(@aria-disabled='true')]")));
                js.executeScript("arguments[0].click();", altPostBtn);
                System.out.println("📢 Click nút Đăng thành công (fallback).");
            } catch (Exception e2) {
                throw new RuntimeException("❌ Không tìm thấy hoặc không thể click nút Đăng: " + e2.getMessage());
            }
        }
    }

    /**
     * Lấy URL hiện tại làm link bài đăng (sau khi post xong)
     */
    public String getPostLink(WebDriver driver) throws InterruptedException {
        Thread.sleep(6000);
        return driver.getCurrentUrl();
    }
}
