package vn.system.app.modules.facebook.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class FacebookPostService {

    private static final String GRAPH_API_URL = "https://graph.facebook.com/v21.0";
    private final RestTemplate restTemplate = new RestTemplate();

    // ============================================================
    // 1️ Đăng video thực tế lên fanpage
    // ============================================================
    public Map<String, Object> postVideoToPage(String pageId, String message, String filePath, String accessToken) {
        Map<String, Object> result = new HashMap<>();
        result.put("pageId", pageId);
        result.put("message", message);
        result.put("filePath", filePath);

        try {
            String url = GRAPH_API_URL + "/" + pageId + "/videos";

            File file = new File(filePath);
            if (!file.exists()) {
                throw new RuntimeException("Không tìm thấy file video: " + filePath);
            }

            MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
            params.add("description", message);
            params.add("access_token", accessToken);
            params.add("file", new FileSystemResource(file));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            log.info("[FACEBOOK VIDEO UPLOAD] PageID: {}", pageId);
            log.info(" Request Params: {}", params);
            log.info(" Response Status: {}", response.getStatusCode());
            log.info(" Response Body: {}", response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null
                    && response.getBody().contains("id")) {
                log.info(" Đăng video thành công lên fanpage: {}", pageId);
                result.put("success", true);
                result.put("response", response.getBody());
            } else {
                log.error(" Facebook trả lỗi khi upload video: {}", response.getBody());
                result.put("success", false);
                result.put("error", response.getBody());
            }

        } catch (HttpClientErrorException e) {
            log.error(" Lỗi HTTP {} khi upload video: {}", e.getStatusCode(), e.getResponseBodyAsString());
            result.put("success", false);
            result.put("error", e.getResponseBodyAsString());

        } catch (Exception e) {
            log.error(" Lỗi không xác định khi upload video: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    // ============================================================
    // 2 Đăng bài viết có caption/link lên fanpage (feed)
    // ============================================================
    public Map<String, Object> postToPage(String pageId, String message, String link, String accessToken) {
        Map<String, Object> result = new HashMap<>();
        result.put("pageId", pageId);
        result.put("link", link);
        result.put("message", message);

        try {
            String url = GRAPH_API_URL + "/" + pageId + "/feed";

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("message", message);
            if (link != null && !link.isBlank()) {
                params.add("link", link);
            }
            params.add("access_token", accessToken);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            log.info(" [FACEBOOK FEED POST] PageID: {}", pageId);
            log.info(" Request Params: {}", params);
            log.info(" Response Status: {}", response.getStatusCode());
            log.info(" Response Body: {}", response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null
                    && response.getBody().contains("id")) {
                log.info(" Đăng bài thành công lên fanpage: {}", pageId);
                result.put("success", true);
                result.put("response", response.getBody());
            } else {
                log.error(" Facebook trả lỗi khi đăng bài: {}", response.getBody());
                result.put("success", false);
                result.put("error", response.getBody());
            }

        } catch (HttpClientErrorException e) {
            log.error(" Facebook API lỗi HTTP {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            result.put("success", false);
            result.put("error", e.getResponseBodyAsString());

        } catch (Exception e) {
            log.error(" Lỗi không xác định khi gọi Facebook API: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }
}
