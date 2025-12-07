package vn.system.app.modules.facebook.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.system.app.modules.facebook.service.FacebookPostService;
import vn.system.app.modules.facebook.repository.FacebookPageRepository;
import vn.system.app.modules.sourcelink.service.SourceGroupService;
import vn.system.app.modules.sourcelink.domain.SourceGroup;
import vn.system.app.modules.sourcelink.domain.SourceLink;
import vn.system.app.modules.facebook.domain.FacebookPage;

import java.io.File;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/facebook-post")
public class FacebookPostController {

    private final FacebookPostService facebookPostService;
    private final FacebookPageRepository facebookPageRepo;
    private final SourceGroupService groupService;

    // ✅ Lấy đường dẫn base từ application.properties
    @Value("${hoidanit.upload-file.base-uri}")
    private String uploadBasePath;

    public FacebookPostController(
            FacebookPostService facebookPostService,
            FacebookPageRepository facebookPageRepo,
            SourceGroupService groupService) {
        this.facebookPostService = facebookPostService;
        this.facebookPageRepo = facebookPageRepo;
        this.groupService = groupService;
    }

    /**
     * ============================================================
     * API: Đăng ngay 1 link lên fanpage (video hoặc bài viết)
     * ============================================================
     * Body JSON:
     * {
     * "groupId": 5,
     * "linkId": 12,
     * "pageId": 3
     * }
     */
    @PostMapping("/direct")
    public ResponseEntity<?> postDirect(@RequestBody Map<String, Long> req) {
        Long groupId = req.get("groupId");
        Long linkId = req.get("linkId");
        Long pageId = req.get("pageId");

        // ✅ 1. Lấy thông tin Group & Link
        SourceGroup group = groupService.findById(groupId);
        SourceLink link = group.getLinks().stream()
                .filter(l -> l.getId().equals(linkId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy link ID = " + linkId + " trong group này!"));

        // ✅ 2. Lấy thông tin Fanpage
        FacebookPage page = facebookPageRepo.findById(pageId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy fanpage ID = " + pageId));

        String accessToken = page.getAccessToken();
        String pageRealId = page.getPageId();

        // ✅ 3. Tạo caption
        String caption = "📢 " + Optional.ofNullable(link.getCaption()).orElse("(Không có mô tả)")
                + "\n\n👤 Nguồn: " + Optional.ofNullable(link.getUserId()).orElse("Ẩn danh");

        // ✅ 4. Xử lý base path đúng (file:///D:/... -> D:/...)
        String basePath = uploadBasePath
                .replace("file:///", "")
                .replace("file://", "")
                .replace("/", File.separator);

        // ✅ 5. Gắn đúng thư mục con threads_video
        String videoFileName = link.getContentGenerated();
        if (videoFileName == null || videoFileName.isBlank()) {
            return ResponseEntity.status(400).body(Map.of(
                    "status", "error",
                    "message", "Link chưa có file video (contentGenerated rỗng)"));
        }

        // Nếu fileName chưa có thư mục threads_video → tự thêm vào
        if (!videoFileName.contains("threads_video")) {
            videoFileName = "threads_video" + File.separator + videoFileName;
        }

        String fullPath = basePath + File.separator + videoFileName;
        File file = new File(fullPath);

        if (!file.exists()) {
            return ResponseEntity.status(400).body(Map.of(
                    "status", "error",
                    "message", "Không tìm thấy file video để đăng: " + fullPath));
        }

        // ✅ 6. Gọi Facebook API để đăng video
        Map<String, Object> result = facebookPostService.postVideoToPage(
                pageRealId,
                caption,
                fullPath,
                accessToken);

        // ✅ 7. Trả kết quả về client
        boolean success = result.getOrDefault("success", false).equals(true);

        return ResponseEntity.ok(Map.of(
                "statusCode", 200,
                "message", success ? "🎉 Đăng bài thành công lên fanpage!" : "❌ Đăng bài thất bại!",
                "group", group.getName(),
                "page", page.getName(),
                "linkId", linkId,
                "pageRealId", pageRealId,
                "filePath", fullPath,
                "result", result));
    }
}
