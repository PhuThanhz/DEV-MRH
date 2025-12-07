package vn.system.app.modules.facebook.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import vn.system.app.modules.facebook.domain.FacebookScheduleSetting;
import vn.system.app.modules.facebook.repository.FacebookScheduleSettingRepository;
import vn.system.app.modules.sourcelink.domain.SourceGroup;
import vn.system.app.modules.sourcelink.domain.SourceLink;
import vn.system.app.modules.sourcelink.service.SourceGroupService;

import java.io.File;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class FacebookSchedulerService {

    private final FacebookScheduleSettingRepository settingRepo;
    private final FacebookPostService facebookPostService;
    private final SourceGroupService groupService;
    private final TaskScheduler scheduler;

    @Value("${hoidanit.upload-file.base-uri}")
    private String uploadBasePath;

    public FacebookSchedulerService(
            FacebookScheduleSettingRepository settingRepo,
            FacebookPostService facebookPostService,
            SourceGroupService groupService,
            TaskScheduler scheduler) {
        this.settingRepo = settingRepo;
        this.facebookPostService = facebookPostService;
        this.groupService = groupService;
        this.scheduler = scheduler;
    }

    // ============================================================
    // 🕒 Sinh lịch thực tế từ cấu hình đã lưu
    // ============================================================
    public Map<String, Object> generateSchedule(Long groupId, Instant startTime, Integer intervalMinutes,
            String orderedJson) {

        // 1️⃣ Lấy group và kiểm tra
        SourceGroup group = groupService.findById(groupId);
        if (group == null) {
            throw new RuntimeException("Không tìm thấy group ID = " + groupId);
        }

        // 2️⃣ Lấy cấu hình (bao gồm fanpage)
        FacebookScheduleSetting setting = settingRepo.findBySourceGroup_Id(groupId);
        if (setting == null || setting.getFacebookPage() == null) {
            throw new RuntimeException("Group này chưa được gán fanpage trong cấu hình!");
        }

        var page = setting.getFacebookPage();
        log.info("📆 Bắt đầu sinh lịch đăng cho group '{}' -> fanpage '{}'", group.getName(), page.getName());

        // 3️⃣ Xử lý danh sách ID link theo thứ tự (nếu có)
        List<Long> orderedIds = new ArrayList<>();
        if (orderedJson != null && !orderedJson.isBlank()) {
            orderedIds = Arrays.stream(orderedJson
                    .replace("[", "")
                    .replace("]", "")
                    .split(","))
                    .filter(s -> !s.isBlank())
                    .map(Long::valueOf)
                    .toList();
        }

        // 4️⃣ Lấy danh sách link cần đăng theo thứ tự
        List<SourceLink> links = new ArrayList<>();
        if (!orderedIds.isEmpty()) {
            for (Long id : orderedIds) {
                group.getLinks().stream()
                        .filter(l -> l.getId().equals(id))
                        .findFirst()
                        .ifPresent(links::add);
            }
        } else {
            links.addAll(group.getLinks());
        }

        if (links.isEmpty()) {
            throw new RuntimeException("Group này không có link nào để đăng!");
        }

        // 5️⃣ Mặc định khoảng cách 10 phút nếu chưa có
        if (intervalMinutes == null || intervalMinutes <= 0) {
            intervalMinutes = 10;
        }

        // 6️⃣ Xử lý base path chuẩn (file:/// → D:/...)
        String basePath = uploadBasePath
                .replace("file:///", "")
                .replace("file://", "")
                .replace("/", File.separator);

        // 7️⃣ Tạo danh sách lịch đăng
        List<Map<String, Object>> schedule = new ArrayList<>();
        Instant cursor = startTime;

        for (SourceLink link : links) {
            Instant finalCursor = cursor;

            scheduler.schedule(() -> {
                try {
                    // 🧩 Caption
                    String caption = "📢 " + Optional.ofNullable(link.getCaption()).orElse("(Không có mô tả)")
                            + "\n\n👤 Nguồn: " + Optional.ofNullable(link.getUserId()).orElse("Ẩn danh");

                    // 🧩 Xác định đường dẫn video
                    String videoFile = link.getContentGenerated();
                    if (videoFile == null || videoFile.isBlank()) {
                        log.warn("⚠️ Link ID={} không có file video (bỏ qua).", link.getId());
                        return;
                    }

                    if (!videoFile.contains("threads_video")) {
                        videoFile = "threads_video" + File.separator + videoFile;
                    }

                    String fullPath = basePath + File.separator + videoFile;
                    File file = new File(fullPath);
                    if (!file.exists()) {
                        log.error("❌ Không tìm thấy file video: {}", fullPath);
                        return;
                    }

                    // 🧩 Gọi Facebook API
                    var result = facebookPostService.postVideoToPage(
                            page.getPageId(),
                            caption,
                            fullPath,
                            page.getAccessToken());

                    if (Boolean.TRUE.equals(result.get("success"))) {
                        log.info("✅ Đã đăng link ID={} lên page '{}' lúc {}", link.getId(), page.getName(),
                                finalCursor);
                    } else {
                        log.error("❌ Đăng thất bại link ID={} - lỗi: {}", link.getId(), result.get("error"));
                    }

                } catch (Exception e) {
                    log.error("💥 Lỗi khi đăng link ID={}: {}", link.getId(), e.getMessage());
                }
            }, Date.from(finalCursor));

            schedule.add(Map.of(
                    "linkId", link.getId(),
                    "scheduledAt", finalCursor.toString(),
                    "intervalMinutes", intervalMinutes));
            cursor = cursor.plusSeconds(intervalMinutes * 60L);
        }

        log.info("🗓️ Đã sinh {} bài đăng cho group '{}' (bắt đầu từ {})",
                schedule.size(), group.getName(), startTime);

        // 8️⃣ Trả về thông tin kết quả
        return Map.of(
                "group", group.getName(),
                "page", page.getName(),
                "totalPosts", schedule.size(),
                "startTime", startTime,
                "intervalMinutes", intervalMinutes,
                "schedule", schedule);
    }
}
