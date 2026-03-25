package vn.system.app.modules.employeecareerpath.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.careerpath.domain.CareerPath;
import vn.system.app.modules.careerpathtemplate.domain.CareerPathTemplate;
import vn.system.app.modules.careerpathtemplate.domain.CareerPathTemplateStep;
import vn.system.app.modules.careerpathtemplate.service.CareerPathTemplateService;
import vn.system.app.modules.departmentjobtitle.domain.DepartmentJobTitle;
import vn.system.app.modules.departmentjobtitle.repository.DepartmentJobTitleRepository;
import vn.system.app.modules.employeecareerpath.domain.EmployeeCareerPath;
import vn.system.app.modules.employeecareerpath.domain.EmployeeCareerPathHistory;
import vn.system.app.modules.employeecareerpath.domain.request.ReqAssignCareerPathDTO;
import vn.system.app.modules.employeecareerpath.domain.request.ReqPromoteEmployeeDTO;
import vn.system.app.modules.employeecareerpath.domain.response.ResEmployeeCareerPathDTO;
import vn.system.app.modules.employeecareerpath.domain.response.ResEmployeeCareerPathHistoryDTO;
import vn.system.app.modules.employeecareerpath.repository.EmployeeCareerPathHistoryRepository;
import vn.system.app.modules.employeecareerpath.repository.EmployeeCareerPathRepository;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.repository.UserRepository;
import vn.system.app.modules.userposition.domain.UserPosition;
import vn.system.app.modules.userposition.repository.UserPositionRepository;

@Service
public class EmployeeCareerPathService {

    private final EmployeeCareerPathRepository repo;
    private final EmployeeCareerPathHistoryRepository historyRepo;
    private final UserRepository userRepo;
    private final CareerPathTemplateService templateService;
    private final UserPositionRepository userPositionRepo;
    private final DepartmentJobTitleRepository departmentJobTitleRepo;

    public EmployeeCareerPathService(
            EmployeeCareerPathRepository repo,
            EmployeeCareerPathHistoryRepository historyRepo,
            UserRepository userRepo,
            CareerPathTemplateService templateService,
            UserPositionRepository userPositionRepo,
            DepartmentJobTitleRepository departmentJobTitleRepo) {
        this.repo = repo;
        this.historyRepo = historyRepo;
        this.userRepo = userRepo;
        this.templateService = templateService;
        this.userPositionRepo = userPositionRepo;
        this.departmentJobTitleRepo = departmentJobTitleRepo;
    }

    // =====================================================
    // ASSIGN — HR gán lộ trình cho nhân viên
    // =====================================================
    @Transactional
    public ResEmployeeCareerPathDTO handleAssign(ReqAssignCareerPathDTO req) {

        if (repo.existsByUser_IdAndActiveTrue(req.getUserId())) {
            throw new IdInvalidException("Nhân viên này đã có lộ trình thăng tiến đang hoạt động");
        }

        User user = userRepo.findById(req.getUserId())
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy nhân viên"));

        CareerPathTemplate template = templateService.fetchById(req.getTemplateId());

        CareerPath currentCp = template.getSteps().stream()
                .filter(s -> s.getCareerPath().getId().equals(req.getCurrentCareerPathId()))
                .map(CareerPathTemplateStep::getCareerPath)
                .findFirst()
                .orElseThrow(() -> new IdInvalidException(
                        "Chức danh hiện tại của nhân viên không có trong lộ trình này"));

        if (!currentCp.isActive()) {
            throw new IdInvalidException(
                    "Chức danh '" + currentCp.getJobTitle().getNameVi() + "' đã bị vô hiệu hoá");
        }

        CareerPathTemplateStep startStep = template.getSteps().stream()
                .filter(s -> s.getCareerPath().getId().equals(req.getCurrentCareerPathId()))
                .findFirst()
                .orElseThrow(() -> new IdInvalidException(
                        "Không tìm thấy bước phù hợp trong lộ trình"));

        EmployeeCareerPath e = new EmployeeCareerPath();
        e.setUser(user);
        e.setTemplate(template);
        e.setCurrentStepOrder(startStep.getStepOrder());
        e.setStepStartedAt(req.getStartDate() != null ? req.getStartDate() : LocalDate.now());
        e.setProgressStatus(0);
        e.setNote(req.getNote());
        e.setActive(true);

        return convertToResponse(repo.save(e), true);
    }

    // =====================================================
    // UPDATE — HR cập nhật note
    // =====================================================
    @Transactional
    public ResEmployeeCareerPathDTO handleUpdate(Long id, ReqAssignCareerPathDTO req) {
        EmployeeCareerPath e = fetchById(id);
        if (req.getNote() != null) {
            e.setNote(req.getNote());
        }
        return convertToResponse(repo.save(e), true);
    }

    // =====================================================
    // PROMOTE — lên bước tiếp theo + cập nhật UserPosition
    // =====================================================
    @Transactional
    public ResEmployeeCareerPathDTO handlePromote(Long id, ReqPromoteEmployeeDTO req) {

        EmployeeCareerPath e = fetchById(id);

        CareerPathTemplateStep currentStep = templateService.fetchStep(
                e.getTemplate().getId(), e.getCurrentStepOrder());

        CareerPathTemplateStep nextStep = templateService.fetchNextStep(
                e.getTemplate().getId(), e.getCurrentStepOrder());

        if (nextStep == null) {
            throw new IdInvalidException(
                    "Nhân viên đã ở bước cao nhất trong lộ trình, không thể thăng tiến thêm");
        }

        if (!nextStep.getCareerPath().isActive()) {
            throw new IdInvalidException(
                    "Chức danh '" + nextStep.getCareerPath().getJobTitle().getNameVi()
                            + "' đã bị vô hiệu hoá, không thể thăng tiến");
        }

        LocalDate promotedAt = req.getPromotedAt() != null ? req.getPromotedAt() : LocalDate.now();

        // Lưu lịch sử
        EmployeeCareerPathHistory history = new EmployeeCareerPathHistory();
        history.setEmployeeCareerPath(e);
        history.setFromCareerPath(currentStep.getCareerPath());
        history.setFromStepOrder(currentStep.getStepOrder());
        history.setToCareerPath(nextStep.getCareerPath());
        history.setToStepOrder(nextStep.getStepOrder());
        history.setPromotedAt(promotedAt);
        history.setNote(req.getNote());
        historyRepo.save(history);

        // Cập nhật bước mới
        e.setCurrentStepOrder(nextStep.getStepOrder());
        e.setStepStartedAt(promotedAt);
        e.setProgressStatus(0);

        // Nếu đây là bước cuối → đánh dấu COMPLETED
        CareerPathTemplateStep afterNext = templateService.fetchNextStep(
                e.getTemplate().getId(), nextStep.getStepOrder());
        if (afterNext == null) {
            e.setProgressStatus(1);
        }

        repo.save(e);

        // ✅ Cập nhật UserPosition DEPARTMENT → chức danh mới
        updateUserPosition(e.getUser().getId(), nextStep.getCareerPath());

        return convertToResponse(e, true);
    }

    // =====================================================
    // SET STATUS — tạm dừng / mở lại
    // =====================================================
    @Transactional
    public void handleSetStatus(Long id, Integer status) {
        EmployeeCareerPath e = fetchById(id);
        e.setProgressStatus(status);
        repo.save(e);
    }

    // =====================================================
    // DEACTIVATE — kết thúc lộ trình
    // =====================================================
    @Transactional
    public void handleDeactivate(Long id) {
        EmployeeCareerPath e = fetchById(id);
        e.setActive(false);
        repo.save(e);
    }

    // =====================================================
    // FETCH
    // =====================================================
    public EmployeeCareerPath fetchById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy lộ trình nhân viên"));
    }

    public ResEmployeeCareerPathDTO fetchByUserId(Long userId) {
        EmployeeCareerPath e = repo.findByUser_IdAndActiveTrue(userId)
                .orElseThrow(() -> new IdInvalidException("Nhân viên chưa có lộ trình thăng tiến"));
        return convertToResponse(e, true);
    }

    public List<ResEmployeeCareerPathDTO> fetchByDepartment(Long departmentId) {
        return repo.findByTemplate_Department_IdAndActiveTrue(departmentId)
                .stream()
                .map(e -> convertToResponse(e, false))
                .toList();
    }

    public List<ResEmployeeCareerPathDTO> fetchUpcomingPromotions(int withinDays) {
        LocalDate deadline = LocalDate.now().plusDays(withinDays);

        return repo.findAllInProgress().stream()
                .filter(e -> {
                    CareerPathTemplateStep step = e.getTemplate().getSteps().stream()
                            .filter(s -> s.getStepOrder().equals(e.getCurrentStepOrder()))
                            .findFirst()
                            .orElse(null);

                    if (step == null || step.getDurationMonths() == null
                            || e.getStepStartedAt() == null)
                        return false;

                    LocalDate expectedDate = e.getStepStartedAt()
                            .plusMonths(step.getDurationMonths());
                    return !expectedDate.isAfter(deadline);
                })
                .sorted((a, b) -> {
                    LocalDate dateA = getExpectedDate(a);
                    LocalDate dateB = getExpectedDate(b);
                    if (dateA == null)
                        return 1;
                    if (dateB == null)
                        return -1;
                    return dateA.compareTo(dateB);
                })
                .map(e -> convertToResponse(e, false))
                .toList();
    }

    public List<ResEmployeeCareerPathHistoryDTO> fetchHistory(Long userId) {
        return historyRepo
                .findByEmployeeCareerPath_User_IdOrderByPromotedAtDesc(userId)
                .stream()
                .map(this::convertHistoryToResponse)
                .toList();
    }

    // =====================================================
    // HELPER — cập nhật UserPosition khi thăng tiến
    // =====================================================
    private void updateUserPosition(Long userId, CareerPath newCareerPath) {

        Long departmentId = newCareerPath.getDepartment().getId();
        Long jobTitleId = newCareerPath.getJobTitle().getId();

        // Tìm DepartmentJobTitle tương ứng chức danh mới
        DepartmentJobTitle newDjt = departmentJobTitleRepo
                .findByDepartment_IdAndJobTitle_IdAndActiveTrue(departmentId, jobTitleId)
                .orElse(null);

        if (newDjt == null) {
            // Không crash — chỉ bỏ qua nếu không tìm thấy mapping
            // HR có thể cần tạo thêm DepartmentJobTitle thủ công
            return;
        }

        // Tìm UserPosition DEPARTMENT active của nhân viên
        UserPosition userPosition = userPositionRepo
                .findByUser_IdAndSourceAndActiveTrue(userId, "DEPARTMENT")
                .orElse(null);

        if (userPosition == null) {
            // Không có UserPosition DEPARTMENT → bỏ qua
            return;
        }

        // Cập nhật sang chức danh mới
        userPosition.setDepartmentJobTitle(newDjt);
        userPositionRepo.save(userPosition);
    }

    // =====================================================
    // HELPER — tính ngày dự kiến thăng tiến
    // =====================================================
    private LocalDate getExpectedDate(EmployeeCareerPath e) {
        return e.getTemplate().getSteps().stream()
                .filter(s -> s.getStepOrder().equals(e.getCurrentStepOrder()))
                .findFirst()
                .map(s -> s.getDurationMonths() != null && e.getStepStartedAt() != null
                        ? e.getStepStartedAt().plusMonths(s.getDurationMonths())
                        : null)
                .orElse(null);
    }

    // =====================================================
    // CONVERT
    // =====================================================
    private ResEmployeeCareerPathDTO convertToResponse(
            EmployeeCareerPath e, boolean includeHistory) {

        ResEmployeeCareerPathDTO r = new ResEmployeeCareerPathDTO();
        r.setId(e.getId());

        // User
        ResEmployeeCareerPathDTO.UserInfo userInfo = new ResEmployeeCareerPathDTO.UserInfo();
        userInfo.setId(e.getUser().getId());
        userInfo.setName(e.getUser().getName());
        userInfo.setEmail(e.getUser().getEmail());
        r.setUser(userInfo);

        // Template
        CareerPathTemplate template = e.getTemplate();
        ResEmployeeCareerPathDTO.TemplateInfo templateInfo = new ResEmployeeCareerPathDTO.TemplateInfo();
        templateInfo.setId(template.getId());
        templateInfo.setName(template.getName());
        if (template.getDepartment() != null) {
            templateInfo.setDepartmentId(template.getDepartment().getId());
            templateInfo.setDepartmentName(template.getDepartment().getName());
        }
        r.setTemplate(templateInfo);

        List<CareerPathTemplateStep> steps = template.getSteps();
        r.setTotalSteps(steps.size());
        r.setCurrentStepOrder(e.getCurrentStepOrder());

        // Build promotedAt map: fromStepOrder → promotedAt
        Map<Integer, LocalDate> promotedAtMap = Map.of();
        if (e.getHistories() != null) {
            promotedAtMap = e.getHistories().stream()
                    .filter(h -> h.getFromStepOrder() != null && h.getPromotedAt() != null)
                    .collect(Collectors.toMap(
                            EmployeeCareerPathHistory::getFromStepOrder,
                            EmployeeCareerPathHistory::getPromotedAt,
                            (a, b) -> b));
        }
        final Map<Integer, LocalDate> promotedAtMapFinal = promotedAtMap;

        // Build allSteps
        List<ResEmployeeCareerPathDTO.StepProgress> allSteps = new ArrayList<>();
        for (CareerPathTemplateStep s : steps) {
            ResEmployeeCareerPathDTO.StepProgress sp = new ResEmployeeCareerPathDTO.StepProgress();
            sp.setStepOrder(s.getStepOrder());
            sp.setDurationMonths(s.getDurationMonths());

            CareerPath cp = s.getCareerPath();
            sp.setCareerPathId(cp.getId());
            sp.setJobTitleName(cp.getJobTitle().getNameVi());
            if (cp.getJobTitle().getPositionLevel() != null) {
                sp.setPositionLevelCode(cp.getJobTitle().getPositionLevel().getCode());
            }

            if (s.getStepOrder() < e.getCurrentStepOrder()) {
                sp.setStepStatus("COMPLETED");
                LocalDate pAt = promotedAtMapFinal.get(s.getStepOrder());
                sp.setPromotedAt(pAt);

                if (pAt != null) {
                    LocalDate startOfStep = promotedAtMapFinal.get(s.getStepOrder() - 1);
                    if (startOfStep == null) {
                        startOfStep = e.getStepStartedAt();
                    }
                    if (startOfStep != null) {
                        sp.setActualMonths(ChronoUnit.MONTHS.between(startOfStep, pAt));
                    }
                }
            } else if (s.getStepOrder().equals(e.getCurrentStepOrder())) {
                sp.setStepStatus("CURRENT");
            } else {
                sp.setStepStatus("UPCOMING");
            }

            allSteps.add(sp);
        }
        r.setAllSteps(allSteps);

        // Current step shortcut
        steps.stream()
                .filter(s -> s.getStepOrder().equals(e.getCurrentStepOrder()))
                .findFirst()
                .ifPresent(s -> {
                    r.setCurrentStep(toStepInfo(s));
                    r.setDurationMonths(s.getDurationMonths());
                    if (e.getStepStartedAt() != null && s.getDurationMonths() != null) {
                        LocalDate deadline = e.getStepStartedAt()
                                .plusMonths(s.getDurationMonths());
                        r.setOverdue(LocalDate.now().isAfter(deadline));
                    }
                });

        // Next step shortcut
        CareerPathTemplateStep nextStep = templateService.fetchNextStep(
                template.getId(), e.getCurrentStepOrder());
        if (nextStep != null) {
            r.setNextStep(toStepInfo(nextStep));
        }

        // Số ngày ở bước hiện tại
        r.setStepStartedAt(e.getStepStartedAt());
        if (e.getStepStartedAt() != null) {
            r.setDaysInCurrentStep(
                    ChronoUnit.DAYS.between(e.getStepStartedAt(), LocalDate.now()));
        }

        r.setProgressStatus(e.getProgressStatus());
        r.setProgressStatusLabel(resolveStatusLabel(e.getProgressStatus()));
        r.setNote(e.getNote());
        r.setActive(e.isActive());

        if (includeHistory && e.getHistories() != null) {
            r.setHistories(e.getHistories().stream()
                    .map(this::convertHistoryToResponse)
                    .toList());
        }

        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        r.setCreatedBy(e.getCreatedBy());
        r.setUpdatedBy(e.getUpdatedBy());

        return r;
    }

    private ResEmployeeCareerPathDTO.StepInfo toStepInfo(CareerPathTemplateStep s) {
        ResEmployeeCareerPathDTO.StepInfo info = new ResEmployeeCareerPathDTO.StepInfo();
        info.setStepOrder(s.getStepOrder());
        info.setDurationMonths(s.getDurationMonths());
        CareerPath cp = s.getCareerPath();
        info.setCareerPathId(cp.getId());
        info.setJobTitleName(cp.getJobTitle().getNameVi());
        if (cp.getJobTitle().getPositionLevel() != null) {
            info.setPositionLevelCode(cp.getJobTitle().getPositionLevel().getCode());
        }
        return info;
    }

    private ResEmployeeCareerPathHistoryDTO convertHistoryToResponse(EmployeeCareerPathHistory h) {
        ResEmployeeCareerPathHistoryDTO r = new ResEmployeeCareerPathHistoryDTO();
        r.setId(h.getId());

        r.setFromStepOrder(h.getFromStepOrder());
        if (h.getFromCareerPath() != null) {
            r.setFromPositionCode(
                    h.getFromCareerPath().getJobTitle().getPositionLevel().getCode());
            r.setFromPositionName(h.getFromCareerPath().getJobTitle().getNameVi());
        }

        r.setToStepOrder(h.getToStepOrder());
        if (h.getToCareerPath() != null) {
            r.setToPositionCode(
                    h.getToCareerPath().getJobTitle().getPositionLevel().getCode());
            r.setToPositionName(h.getToCareerPath().getJobTitle().getNameVi());
        }

        r.setPromotedAt(h.getPromotedAt());
        r.setNote(h.getNote());
        r.setCreatedAt(h.getCreatedAt());
        r.setCreatedBy(h.getCreatedBy());
        return r;
    }

    private String resolveStatusLabel(Integer status) {
        if (status == null)
            return "Không xác định";
        return switch (status) {
            case 0 -> "Đang tiến hành";
            case 1 -> "Đã hoàn thành lộ trình";
            case 2 -> "Tạm dừng";
            default -> "Không xác định";
        };
    }
}