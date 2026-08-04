package vn.system.app.startup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import vn.system.app.modules.jd.jobdescriptionrequirement.domain.JobDescriptionRequirement;
import vn.system.app.modules.jd.jobdescriptionrequirement.domain.JobDescriptionRequirementItem;
import vn.system.app.modules.jd.jobdescriptionrequirement.domain.enums.RequirementCategory;
import vn.system.app.modules.jd.jobdescriptionrequirement.repository.JobDescriptionRequirementItemRepository;
import vn.system.app.modules.jd.jobdescriptionrequirement.repository.JobDescriptionRequirementRepository;
import vn.system.app.modules.jd.jobdescriptiontask.domain.JobDescriptionTask;
import vn.system.app.modules.jd.jobdescriptiontask.domain.JobDescriptionTaskItem;
import vn.system.app.modules.jd.jobdescriptiontask.repository.JobDescriptionTaskItemRepository;
import vn.system.app.modules.jd.jobdescriptiontask.repository.JobDescriptionTaskRepository;

/**
 * Tách dữ liệu text tự do cũ (content/knowledge/experience/skills/qualities/otherRequirements)
 * thành các record con (job_description_task_items, job_description_requirement_items)
 * để mỗi dòng "-" có id riêng — phục vụ đánh số 1.1/1.2 và cho Task liên kết tới mục con.
 *
 * Idempotent: mỗi record cha chỉ được backfill nếu chưa có item con nào, nên chạy lại
 * nhiều lần (mỗi lần khởi động) không tạo trùng dữ liệu. Có thể xoá runner này sau khi
 * mọi môi trường (dev/staging/prod) đã khởi động ít nhất 1 lần kể từ khi deploy tính năng này.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(2) // chạy sau DatabaseInitializer (@Order(1)) — cần seed data đã tồn tại trước khi tách sang bảng item
public class JobDescriptionItemBackfillRunner implements ApplicationRunner {

    private final JobDescriptionTaskRepository taskRepository;
    private final JobDescriptionTaskItemRepository taskItemRepository;
    private final JobDescriptionRequirementRepository requirementRepository;
    private final JobDescriptionRequirementItemRepository requirementItemRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        backfillTasks();
        backfillRequirements();
        log.info("[JD Item Backfill] Hoàn tất toàn bộ!");
    }

    private void backfillTasks() {
        List<JobDescriptionTask> tasks = taskRepository.findAll();
        int migrated = 0;

        for (JobDescriptionTask task : tasks) {
            boolean hasItems = !taskItemRepository.findByJobDescriptionTask_IdOrderByOrderNo(task.getId()).isEmpty();
            if (hasItems || task.getContent() == null || task.getContent().isBlank()) {
                continue;
            }

            List<String> lines = splitLines(task.getContent());
            if (lines.isEmpty()) {
                continue;
            }

            List<JobDescriptionTaskItem> items = new ArrayList<>();
            int order = 1;
            for (String line : lines) {
                JobDescriptionTaskItem item = new JobDescriptionTaskItem();
                item.setJobDescriptionTask(task);
                item.setOrderNo(order++);
                item.setContent(line);
                items.add(item);
            }
            taskItemRepository.saveAll(items);
            migrated++;
        }

        if (migrated > 0) {
            log.info("[JD Item Backfill] JobDescriptionTask: đã tách {} record sang job_description_task_items", migrated);
        } else {
            log.info("[JD Item Backfill] JobDescriptionTask: không có record nào cần backfill");
        }
    }

    private void backfillRequirements() {
        List<JobDescriptionRequirement> requirements = requirementRepository.findAll();
        int migrated = 0;

        for (JobDescriptionRequirement requirement : requirements) {
            boolean hasItems = !requirementItemRepository
                    .findByJobDescriptionRequirement_IdOrderByCategoryAscOrderNoAsc(requirement.getId()).isEmpty();
            if (hasItems) {
                continue;
            }

            List<JobDescriptionRequirementItem> items = new ArrayList<>();
            items.addAll(buildItems(requirement, RequirementCategory.KNOWLEDGE, requirement.getKnowledge()));
            items.addAll(buildItems(requirement, RequirementCategory.EXPERIENCE, requirement.getExperience()));
            items.addAll(buildItems(requirement, RequirementCategory.SKILLS, requirement.getSkills()));
            items.addAll(buildItems(requirement, RequirementCategory.QUALITIES, requirement.getQualities()));
            items.addAll(buildItems(requirement, RequirementCategory.OTHER, requirement.getOtherRequirements()));

            if (items.isEmpty()) {
                continue;
            }

            requirementItemRepository.saveAll(items);
            migrated++;
        }

        if (migrated > 0) {
            log.info("[JD Item Backfill] JobDescriptionRequirement: đã tách {} record sang job_description_requirement_items", migrated);
        } else {
            log.info("[JD Item Backfill] JobDescriptionRequirement: không có record nào cần backfill");
        }
    }

    private List<JobDescriptionRequirementItem> buildItems(
            JobDescriptionRequirement requirement, RequirementCategory category, String rawText) {

        List<JobDescriptionRequirementItem> items = new ArrayList<>();
        List<String> lines = splitLines(rawText);
        int order = 1;
        for (String line : lines) {
            JobDescriptionRequirementItem item = new JobDescriptionRequirementItem();
            item.setJobDescriptionRequirement(requirement);
            item.setCategory(category);
            item.setOrderNo(order++);
            item.setContent(line);
            items.add(item);
        }
        return items;
    }

    /** Tách text tự do thành các dòng: theo xuống dòng hoặc dấu "•", strip tiền tố "- ". */
    private List<String> splitLines(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return List.of();
        }
        return Arrays.stream(rawText.split("\\r?\\n|•"))
                .map(String::trim)
                .map(line -> line.replaceFirst("^-\\s*", ""))
                .filter(line -> !line.isBlank())
                .toList();
    }
}
