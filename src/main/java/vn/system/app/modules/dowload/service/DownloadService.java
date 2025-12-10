package vn.system.app.modules.dowload.service;

import lombok.extern.slf4j.Slf4j;
import vn.system.app.modules.dowload.domain.response.DownloadResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.json.JSONArray;
import org.json.JSONObject;
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

        try {
            // Nếu là link X / Twitter → xử lý riêng
            if (url.contains("x.com") || url.contains("twitter.com")) {
                return processTwitterDownload(url, folder);
            }

            // === Threads (Instagram Threads) ===
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

                // ==== 1️ Lấy link video (nếu có) ====
                Element videoEl = doc.selectFirst("video[src]");
                String mediaUrl = videoEl != null ? videoEl.attr("src") : extractMeta(doc, "og:video");

                // ==== 2️ Nếu không có video → thử lấy hình ảnh ====
                if (mediaUrl == null || mediaUrl.isBlank()) {
                    mediaUrl = extractMeta(doc, "og:image");
                    if (mediaUrl == null || mediaUrl.isBlank()) {
                        Element imgEl = doc.selectFirst("img[src]");
                        if (imgEl != null)
                            mediaUrl = imgEl.attr("src");
                    }
                }

                // ==== 3️ Lấy caption ====
                String caption = extractMeta(doc, "og:description");
                if (caption == null || caption.isBlank()) {
                    Element metaDesc = doc.selectFirst("meta[name=description]");
                    caption = metaDesc != null ? metaDesc.attr("content") : "(Không có mô tả)";
                }

                // ==== 4️ Lấy tên & userId từ og:title ====
                String rawUser = extractMeta(doc, "og:title");
                String name = null;
                String userId = null;

                if (rawUser != null) {
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

                // ==== 5️ Kiểm tra nếu không có video hoặc ảnh ====
                if (mediaUrl == null || mediaUrl.isBlank()) {
                    res.setSuccess(false);
                    res.setCaption(caption);
                    res.setName(name);
                    res.setUserId(userId);
                    res.setError("Không tìm thấy video hoặc hình ảnh trong link Threads.");
                    return res;
                }

                // ==== 6️ Tạo thư mục lưu ====
                Path basePath = Path.of(baseUri.replace("file:///", ""));
                Path folderPath = basePath.resolve(folder);
                if (!Files.exists(folderPath)) {
                    Files.createDirectories(folderPath);
                }

                // ==== 7️ Xác định loại file (video / ảnh) ====
                String extension = detectExtension(mediaUrl);
                String fileName = "threads_" + System.currentTimeMillis() + extension;
                File outputFile = folderPath.resolve(fileName).toFile();

                // ==== 8️ Tải file về ====
                try (InputStream in = new URL(mediaUrl).openStream();
                        FileOutputStream fos = new FileOutputStream(outputFile)) {
                    in.transferTo(fos);
                }

                log.info(" Đã tải về: {}", outputFile.getAbsolutePath());

                // ==== 9️ Trả kết quả ====
                res.setSuccess(true);
                res.setName(name);
                res.setUserId(userId);
                res.setCaption(caption);
                res.setFolder(fileName);
                res.setError(null);

            } catch (Exception e) {
                res.setSuccess(false);
                res.setError("Lỗi xử lý Threads: " + e.getMessage());
                log.error(" Lỗi tải video/hình ảnh: {}", e.getMessage(), e);
            } finally {
                driver.quit();
            }

        } catch (Exception e) {
            res.setSuccess(false);
            res.setError("Lỗi tổng quát: " + e.getMessage());
        }

        return res;
    }

    // ------------------------------------------------------------
    // Hàm tải video từ X / Twitter
    // ------------------------------------------------------------
    private DownloadResponse processTwitterDownload(String url, String folder) {
        DownloadResponse res = new DownloadResponse();

        try {
            String apiUrl = url
                    .replace("https://x.com/", "https://api.vxtwitter.com/")
                    .replace("https://twitter.com/", "https://api.vxtwitter.com/");

            log.info("API VXTwitter: {}", apiUrl);

            Document doc = Jsoup.connect(apiUrl)
                    .ignoreContentType(true)
                    .timeout(10000)
                    .userAgent("Mozilla/5.0")
                    .ignoreHttpErrors(true)
                    .get();

            JSONObject obj = new JSONObject(doc.text());

            if (!obj.has("media_extended")) {
                res.setSuccess(false);
                res.setError("Tweet không chứa video.");
                return res;
            }

            JSONObject media = obj.getJSONArray("media_extended").getJSONObject(0);
            String videoUrl = media.optString("url", null);

            // 🔍 Chọn video chất lượng cao nhất nếu có
            if (media.has("variants")) {
                JSONArray variants = media.getJSONArray("variants");
                int maxBitrate = -1;
                for (int i = 0; i < variants.length(); i++) {
                    JSONObject v = variants.getJSONObject(i);
                    if (v.has("bitrate")) {
                        int bitrate = v.getInt("bitrate");
                        if (bitrate > maxBitrate) {
                            maxBitrate = bitrate;
                            videoUrl = v.getString("url");
                        }
                    }
                }
            }

            if (videoUrl == null) {
                res.setSuccess(false);
                res.setError("Không tìm thấy link video trong tweet.");
                return res;
            }

            String caption = obj.optString("text", "(Không có mô tả)");
            String userId = obj.optJSONObject("user") != null
                    ? obj.getJSONObject("user").optString("screen_name")
                    : "(Không rõ)";
            String name = obj.optJSONObject("user") != null
                    ? obj.getJSONObject("user").optString("name")
                    : "(Không rõ)";

            // === Tạo thư mục lưu ===
            Path basePath = Path.of(baseUri.replace("file:///", ""));
            Path folderPath = basePath.resolve(folder);
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            String fileName = "x_" + System.currentTimeMillis() + ".mp4";
            File outputFile = folderPath.resolve(fileName).toFile();

            // === Tải video ===
            try (InputStream in = new URL(videoUrl).openStream();
                    FileOutputStream fos = new FileOutputStream(outputFile)) {
                in.transferTo(fos);
            }

            log.info(" Video X (Twitter) đã tải về: {}", outputFile.getAbsolutePath());

            res.setSuccess(true);
            res.setName(name);
            res.setUserId(userId);
            res.setCaption(caption);
            res.setFolder(fileName);
            res.setError(null);

        } catch (Exception e) {
            res.setSuccess(false);
            res.setError("Lỗi tải video từ X: " + e.getMessage());
            log.error(" Lỗi tải video từ X: {}", e.getMessage(), e);
        }

        return res;
    }

    // ------------------------------------------------------------
    // Hàm phụ: Lấy meta property
    // ------------------------------------------------------------
    private String extractMeta(Document doc, String property) {
        Element m = doc.selectFirst("meta[property=" + property + "]");
        return m != null ? m.attr("content") : null;
    }

    // ------------------------------------------------------------
    // Hàm phụ: Xác định phần mở rộng file
    // ------------------------------------------------------------
    private String detectExtension(String mediaUrl) {
        String lower = mediaUrl.toLowerCase();
        if (lower.contains(".mp4"))
            return ".mp4";
        if (lower.contains(".jpg") || lower.contains(".jpeg"))
            return ".jpg";
        if (lower.contains(".png"))
            return ".png";
        if (lower.contains(".gif"))
            return ".gif";
        return ".bin";
    }
}
