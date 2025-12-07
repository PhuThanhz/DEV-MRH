package vn.system.app.modules.dowload.service;

import lombok.extern.slf4j.Slf4j;
import vn.system.app.modules.dowload.domain.reponse.DownloadResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
public class DownloadService {

    @Value("${hoidanit.upload-file.base-uri}")
    private String baseUri;

    public DownloadResponse processDownload(String url, String folder) {
        DownloadResponse res = new DownloadResponse();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get(url);
            Thread.sleep(5000); // Đợi Threads render hoàn tất

            String html = driver.getPageSource();
            Document doc = Jsoup.parse(html);

            // ==== Lấy link video ====
            Element videoEl = doc.selectFirst("video[src]");
            String videoUrl = videoEl != null ? videoEl.attr("src") : extractMeta(doc, "og:video");

            // ==== Lấy caption ====
            String caption = extractMeta(doc, "og:description");
            if (caption == null || caption.isBlank()) {
                Element metaDesc = doc.selectFirst("meta[name=description]");
                caption = metaDesc != null ? metaDesc.attr("content") : "(Không có mô tả)";
            }

            // ==== Lấy tên và userId từ og:title ====
            String rawUser = extractMeta(doc, "og:title");
            String name = null;
            String userId = null;

            if (rawUser != null) {
                // Ví dụ "Ngoc Anh (@minirabit_)"
                if (rawUser.contains("(") && rawUser.contains(")")) {
                    int open = rawUser.indexOf("(");
                    int close = rawUser.lastIndexOf(")");
                    name = rawUser.substring(0, open).trim();
                    userId = rawUser.substring(open + 1, close).trim();
                } else if (rawUser.contains("@")) {
                    userId = rawUser.trim();
                } else {
                    name = rawUser.trim();
                }
            }

            // ==== Nếu không có video thì báo lỗi ====
            if (videoUrl == null) {
                res.setSuccess(false);
                res.setCaption(caption);
                res.setName(name);
                res.setUserId(userId);
                res.setError("Không tìm thấy video trong link Threads.");
                return res;
            }

            // ==== Tạo thư mục lưu ====
            Path basePath = Path.of(baseUri.replace("file:///", ""));
            Path folderPath = basePath.resolve(folder);
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            // ==== Tạo tên file video ====
            String fileName = "threads_" + System.currentTimeMillis() + ".mp4";
            File outputFile = folderPath.resolve(fileName).toFile();

            // ==== Tải video ====
            try (InputStream in = new URL(videoUrl).openStream();
                    FileOutputStream fos = new FileOutputStream(outputFile)) {
                in.transferTo(fos);
            }

            log.info("✅ Video đã tải về: {}", outputFile.getAbsolutePath());

            // ==== Lưu kết quả (chỉ lưu file, không lưu URL) ====
            res.setSuccess(true);
            res.setName(name);
            res.setUserId(userId);
            res.setCaption(caption);
            res.setFolder(fileName);
            res.setError(null);

        } catch (Exception e) {
            res.setSuccess(false);
            res.setError("Lỗi xử lý: " + e.getMessage());
            log.error(" Lỗi tải video: {}", e.getMessage(), e);
        } finally {
            driver.quit();
        }

        return res;
    }

    private String extractMeta(Document doc, String property) {
        Element m = doc.selectFirst("meta[property=" + property + "]");
        return m != null ? m.attr("content") : null;
    }
}
