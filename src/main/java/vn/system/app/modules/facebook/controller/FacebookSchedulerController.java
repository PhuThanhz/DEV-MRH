package vn.system.app.modules.facebook.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.system.app.modules.facebook.domain.FacebookScheduleSetting;
import vn.system.app.modules.facebook.repository.FacebookScheduleSettingRepository;
import vn.system.app.modules.facebook.service.FacebookSchedulerService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/facebook-schedule")
public class FacebookSchedulerController {

    private final FacebookScheduleSettingRepository settingRepo;
    private final FacebookSchedulerService schedulerService;

    public FacebookSchedulerController(FacebookScheduleSettingRepository settingRepo,
            FacebookSchedulerService schedulerService) {
        this.settingRepo = settingRepo;
        this.schedulerService = schedulerService;
    }

    @PostMapping("/group/{groupId}")
    public ResponseEntity<?> generateSchedule(@PathVariable("groupId") Long groupId) {
        FacebookScheduleSetting setting = settingRepo.findBySourceGroup_Id(groupId);
        if (setting == null)
            throw new RuntimeException("Group chưa có cấu hình lịch đăng!");

        Map<String, Object> result = schedulerService.generateSchedule(
                groupId,
                setting.getStartTime(),
                setting.getIntervalMinutes(),
                setting.getOrderedLinkIds());

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "result", result));
    }
}
