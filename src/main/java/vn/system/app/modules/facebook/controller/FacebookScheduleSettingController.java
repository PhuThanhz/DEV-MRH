package vn.system.app.modules.facebook.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.system.app.modules.facebook.domain.FacebookScheduleSetting;
import vn.system.app.modules.facebook.repository.FacebookScheduleSettingRepository;
import vn.system.app.modules.facebook.repository.FacebookPageRepository;
import vn.system.app.modules.sourcelink.service.SourceGroupService;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/facebook-schedule-settings")
public class FacebookScheduleSettingController {

    private final FacebookScheduleSettingRepository settingRepo;
    private final SourceGroupService groupService;
    private final FacebookPageRepository pageRepo;

    public FacebookScheduleSettingController(FacebookScheduleSettingRepository settingRepo,
            SourceGroupService groupService,
            FacebookPageRepository pageRepo) {
        this.settingRepo = settingRepo;
        this.groupService = groupService;
        this.pageRepo = pageRepo;
    }

    // ============================================================
    // 2 Gán fanpage cho group
    // ============================================================
    @PutMapping("/assign-page")
    public ResponseEntity<?> assignPage(@RequestBody Map<String, Long> req) {
        Long groupId = req.get("groupId");
        Long pageId = req.get("pageId");

        var group = groupService.findById(groupId);
        var page = pageRepo.findById(pageId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy fanpage"));

        var setting = settingRepo.findBySourceGroup_Id(groupId);
        if (setting == null)
            setting = new FacebookScheduleSetting();

        setting.setSourceGroup(group);
        setting.setFacebookPage(page);
        setting.setUpdatedAt(Instant.now());
        settingRepo.save(setting);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "group", group.getName(),
                "page", page.getName()));
    }

    // ============================================================
    // 3 Cập nhật thứ tự link
    // ============================================================
    @PutMapping("/ordered-links")
    public ResponseEntity<?> updateOrderedLinks(@RequestBody Map<String, String> req) {
        Long groupId = Long.parseLong(req.get("groupId"));
        String ordered = req.get("orderedLinkIds");

        var setting = settingRepo.findBySourceGroup_Id(groupId);
        if (setting == null)
            throw new RuntimeException("Chưa có cấu hình cho group này!");

        setting.setOrderedLinkIds(ordered);
        setting.setUpdatedAt(Instant.now());
        settingRepo.save(setting);

        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    // ============================================================
    // 4 Gán giờ bắt đầu
    // ============================================================
    @PutMapping("/start-time")
    public ResponseEntity<?> updateStartTime(@RequestBody Map<String, String> req) {
        Long groupId = Long.valueOf(req.get("groupId"));
        Instant startTime = Instant.parse(req.get("startTime"));

        var setting = settingRepo.findBySourceGroup_Id(groupId);
        if (setting == null)
            throw new RuntimeException("Chưa có cấu hình!");

        setting.setStartTime(startTime);
        setting.setUpdatedAt(Instant.now());
        settingRepo.save(setting);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    // ============================================================
    // 5 Cập nhật khoảng cách đăng
    // ============================================================
    @PutMapping("/interval")
    public ResponseEntity<?> updateInterval(@RequestBody Map<String, String> req) {
        Long groupId = Long.valueOf(req.get("groupId"));
        Integer interval = Integer.valueOf(req.get("interval"));

        var setting = settingRepo.findBySourceGroup_Id(groupId);
        if (setting == null)
            throw new RuntimeException("Chưa có cấu hình!");

        setting.setIntervalMinutes(interval);
        setting.setUpdatedAt(Instant.now());
        settingRepo.save(setting);

        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    // ============================================================
    // 6 Lưu cấu hình đầy đủ
    // ============================================================
    @PostMapping
    public ResponseEntity<?> saveFullSetting(@RequestBody FacebookScheduleSetting req) {
        var group = groupService.findById(req.getSourceGroup().getId());
        var page = pageRepo.findById(req.getFacebookPage().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy fanpage"));

        var setting = settingRepo.findBySourceGroup_Id(group.getId());
        if (setting == null)
            setting = new FacebookScheduleSetting();

        setting.setSourceGroup(group);
        setting.setFacebookPage(page);
        setting.setStartTime(req.getStartTime());
        setting.setIntervalMinutes(req.getIntervalMinutes());
        setting.setOrderedLinkIds(req.getOrderedLinkIds());
        setting.setLabel(req.getLabel());
        setting.setUpdatedAt(Instant.now());

        settingRepo.save(setting);
        return ResponseEntity.ok(Map.of("status", "saved"));
    }

    // ============================================================
    // Lấy cấu hình hiện tại
    // ============================================================
    @GetMapping("/{groupId}")
    public ResponseEntity<?> getSettingByGroup(@PathVariable Long groupId) {
        var setting = settingRepo.findBySourceGroup_Id(groupId);
        return ResponseEntity.ok(setting);
    }
}
