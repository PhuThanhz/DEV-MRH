package vn.system.app.modules.evaluation.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.evaluation.domain.*;
import vn.system.app.modules.evaluation.domain.enums.*;
import vn.system.app.modules.evaluation.repository.*;
import vn.system.app.modules.notification.service.NotificationService;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.repository.UserRepository;

/**
 * Service quản lý Kỳ đánh giá (Period) và nhân viên tham gia kỳ.
 * Giai đoạn 0: Admin chuẩn bị kỳ + kích hoạt.
 */
@Service
public class EvaluationPeriodService {

    private final EvaluationPeriodRepository periodRepo;
    private final PeriodTemplateRepository periodTemplateRepo;
    private final PeriodEmployeeRepository periodEmployeeRepo;
    private final EvaluationTemplateRepository templateRepo;
    private final EvaluationRecordRepository recordRepo;
    private final NotificationService notificationService;
    private final UserRepository userRepo;

    public EvaluationPeriodService(
            EvaluationPeriodRepository periodRepo,
            PeriodTemplateRepository periodTemplateRepo,
            PeriodEmployeeRepository periodEmployeeRepo,
            EvaluationTemplateRepository templateRepo,
            EvaluationRecordRepository recordRepo,
            NotificationService notificationService,
            UserRepository userRepo) {
        this.periodRepo = periodRepo;
        this.periodTemplateRepo = periodTemplateRepo;
        this.periodEmployeeRepo = periodEmployeeRepo;
        this.templateRepo = templateRepo;
        this.recordRepo = recordRepo;
        this.notificationService = notificationService;
        this.userRepo = userRepo;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PERIOD CRUD
    // ═══════════════════════════════════════════════════════════════════════════

    @Transactional
    public EvaluationPeriod createPeriod(EvaluationPeriod period) {
        validatePeriodDates(period);
        period.setStatus(PeriodStatus.DRAFT);
        return periodRepo.save(period);
    }

    @Transactional
    public EvaluationPeriod updatePeriod(Long id, EvaluationPeriod updates) {
        EvaluationPeriod existing = fetchPeriodById(id);
        checkPeriodEditable(existing);

        if (updates.getName() != null) existing.setName(updates.getName());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getEmployeeStartDate() != null) existing.setEmployeeStartDate(updates.getEmployeeStartDate());
        if (updates.getEmployeeDeadline() != null) existing.setEmployeeDeadline(updates.getEmployeeDeadline());
        if (updates.getManagerDeadline() != null) existing.setManagerDeadline(updates.getManagerDeadline());
        if (updates.getApprovalDeadline() != null) existing.setApprovalDeadline(updates.getApprovalDeadline());

        validatePeriodDates(existing);
        return periodRepo.save(existing);
    }

    public EvaluationPeriod fetchPeriodById(Long id) {
        return periodRepo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Kỳ đánh giá không tồn tại"));
    }

    public ResultPaginationDTO fetchAllPeriods(Specification<EvaluationPeriod> spec, Pageable pageable) {
        Page<EvaluationPeriod> page = periodRepo.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        rs.setMeta(meta);
        rs.setResult(page.getContent());
        return rs;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PERIOD TEMPLATE (liên kết template cho kỳ)
    // ═══════════════════════════════════════════════════════════════════════════

    @Transactional
    public PeriodTemplate addTemplateToPeriod(Long periodId, Long templateId, TemplateType applyToRole) {
        EvaluationPeriod period = fetchPeriodById(periodId);
        checkPeriodEditable(period);

        EvaluationTemplate template = templateRepo.findById(templateId)
                .orElseThrow(() -> new IdInvalidException("Template không tồn tại"));

        if (template.getStatus() != TemplateStatus.ACTIVE) {
            throw new IdInvalidException("Chỉ có thể dùng template đã ACTIVE");
        }

        PeriodTemplate pt = new PeriodTemplate();
        pt.setPeriod(period);
        pt.setTemplate(template);
        pt.setApplyToRole(applyToRole);
        return periodTemplateRepo.save(pt);
    }

    public List<PeriodTemplate> fetchTemplatesByPeriod(Long periodId) {
        return periodTemplateRepo.findByPeriodId(periodId);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PERIOD EMPLOYEES (add nhân viên vào kỳ)
    // ═══════════════════════════════════════════════════════════════════════════

    @Transactional
    public PeriodEmployee addEmployeeToPeriod(Long periodId, String employeeId,
            String directManagerId, Long templateId) {

        EvaluationPeriod period = fetchPeriodById(periodId);
        checkPeriodEditable(period);

        if (periodEmployeeRepo.existsByPeriodIdAndEmployeeId(periodId, employeeId)) {
            throw new IdInvalidException("Nhân viên đã tồn tại trong kỳ đánh giá này");
        }

        User employee = userRepo.findById(employeeId)
                .orElseThrow(() -> new IdInvalidException("Nhân viên không tồn tại"));
        User directManager = userRepo.findById(directManagerId)
                .orElseThrow(() -> new IdInvalidException("Quản lý trực tiếp không tồn tại"));
                
        // Tự động xác định quản lý gián tiếp là cấp trên của quản lý trực tiếp
        User indirectManager = directManager.getDirectManager();
        if (indirectManager == null) {
            throw new IdInvalidException("Quản lý trực tiếp chưa được thiết lập cấp trên, không thể xác định quản lý gián tiếp.");
        }
        EvaluationTemplate template = templateRepo.findById(templateId)
                .orElseThrow(() -> new IdInvalidException("Template không tồn tại"));

        PeriodEmployee pe = new PeriodEmployee();
        pe.setPeriod(period);
        pe.setEmployee(employee);
        pe.setDirectManager(directManager);
        pe.setIndirectManager(indirectManager);
        pe.setTemplate(template);
        pe.setStatus(PeriodEmployeeStatus.ACTIVE);
        return periodEmployeeRepo.save(pe);
    }

    /** Hủy bản đánh giá khi nhân viên nghỉ việc — KHÔNG xóa dữ liệu */
    @Transactional
    public PeriodEmployee cancelEmployee(Long periodEmployeeId) {
        PeriodEmployee pe = periodEmployeeRepo.findById(periodEmployeeId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy nhân viên trong kỳ"));

        pe.setStatus(PeriodEmployeeStatus.CANCELLED);
        return periodEmployeeRepo.save(pe);
    }

    public List<PeriodEmployee> fetchEmployeesByPeriod(Long periodId) {
        return periodEmployeeRepo.findByPeriodId(periodId);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // KÍCH HOẠT KỲ ĐÁNH GIÁ
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Admin kích hoạt kỳ: DRAFT → ACTIVE.
     * Sinh evaluation_record cho từng nhân viên ACTIVE trong kỳ.
     * Gửi thông báo đến tất cả nhân viên.
     */
    @Transactional
    public EvaluationPeriod activatePeriod(Long periodId) {
        EvaluationPeriod period = fetchPeriodById(periodId);

        if (period.getStatus() != PeriodStatus.DRAFT) {
            throw new IdInvalidException("Chỉ có thể kích hoạt kỳ ở trạng thái DRAFT");
        }

        // Validate kỳ có nhân viên
        List<PeriodEmployee> employees = periodEmployeeRepo.findByPeriodIdAndStatus(
                periodId, PeriodEmployeeStatus.ACTIVE);
        if (employees.isEmpty()) {
            throw new IdInvalidException("Kỳ đánh giá chưa có nhân viên nào");
        }

        // Validate kỳ có template
        List<PeriodTemplate> templates = periodTemplateRepo.findByPeriodId(periodId);
        if (templates.isEmpty()) {
            throw new IdInvalidException("Kỳ đánh giá chưa gắn template nào");
        }

        // Validate deadlines
        if (period.getEmployeeStartDate() == null || period.getEmployeeDeadline() == null
                || period.getManagerDeadline() == null || period.getApprovalDeadline() == null) {
            throw new IdInvalidException("Vui lòng thiết lập đầy đủ các mốc deadline trước khi kích hoạt");
        }

        // Sinh evaluation_record cho từng nhân viên
        for (PeriodEmployee pe : employees) {
            EvaluationRecord record = new EvaluationRecord();
            record.setPeriod(period);
            record.setEmployee(pe.getEmployee());
            record.setDirectManager(pe.getDirectManager());
            record.setIndirectManager(pe.getIndirectManager());
            record.setTemplate(pe.getTemplate());
            record.setStatus(RecordStatus.EMPLOYEE_DRAFTING); // Bắt đầu luôn
            recordRepo.save(record);

            // Gửi thông báo cho nhân viên
            sendNotification(pe.getEmployee(), "PERIOD_OPENED", String.format("Kỳ đánh giá \"%s\" đã mở. Vui lòng hoàn thành tự đánh giá trước deadline.", period.getName()), "/evaluation/my-records");
        }

        period.setStatus(PeriodStatus.ACTIVE);
        return periodRepo.save(period);
    }

    /** Đóng kỳ đánh giá: ACTIVE → CLOSED */
    @Transactional
    public EvaluationPeriod closePeriod(Long periodId) {
        EvaluationPeriod period = fetchPeriodById(periodId);

        if (period.getStatus() != PeriodStatus.ACTIVE) {
            throw new IdInvalidException("Chỉ có thể đóng kỳ ở trạng thái ACTIVE");
        }

        period.setStatus(PeriodStatus.CLOSED);
        return periodRepo.save(period);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // VALIDATION
    // ═══════════════════════════════════════════════════════════════════════════

    private void checkPeriodEditable(EvaluationPeriod period) {
        if (period.getStatus() != PeriodStatus.DRAFT) {
            throw new IdInvalidException("Kỳ đánh giá đã kích hoạt hoặc đã đóng, không thể chỉnh sửa");
        }
    }

    private void validatePeriodDates(EvaluationPeriod period) {
        if (period.getEmployeeStartDate() == null || period.getEmployeeDeadline() == null
                || period.getManagerDeadline() == null || period.getApprovalDeadline() == null) {
            return; // allowed to be null in draft before full configuration
        }

        if (period.getEmployeeStartDate().isAfter(period.getEmployeeDeadline())) {
            throw new IdInvalidException("Ngày mở cổng phải diễn ra trước Hạn chót Nhân viên nộp!");
        }
        if (period.getEmployeeDeadline().isAfter(period.getManagerDeadline())) {
            throw new IdInvalidException("Hạn chót Nhân viên nộp phải diễn ra trước Hạn chót Quản lý chấm xong!");
        }
        if (period.getManagerDeadline().isAfter(period.getApprovalDeadline())) {
            throw new IdInvalidException("Hạn chót Quản lý chấm xong phải diễn ra trước Hạn chót Ban lãnh đạo duyệt!");
        }
    }
    private void sendNotification(User recipient, String type, String content, String actionLink) {
        if (recipient == null) return;
        notificationService.sendNotification(recipient.getId(), "EVALUATION", type, content, actionLink);
    }
}
