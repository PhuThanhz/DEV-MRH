package vn.system.app.modules.accountingdossier.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.accountingdossier.domain.AccountingApprovalInstance;
import vn.system.app.modules.accountingdossier.domain.AccountingApprovalWorkflowStep;
import vn.system.app.modules.accountingdossier.domain.AccountingApprovalWorkflowTemplate;
import vn.system.app.modules.accountingdossier.domain.AccountingDossier;
import vn.system.app.modules.accountingdossier.domain.AccountingDossierApprovalStep;
import vn.system.app.modules.accountingdossier.domain.enums.ApprovalStepStatus;
import vn.system.app.modules.accountingdossier.domain.enums.ApproverStrategy;
import vn.system.app.modules.accountingdossier.domain.enums.ApproverType;
import vn.system.app.modules.accountingdossier.domain.enums.WorkflowInstanceStatus;
import vn.system.app.modules.accountingdossier.domain.enums.WorkflowScopeType;
import vn.system.app.modules.accountingdossier.domain.enums.WorkflowTemplateStatus;
import vn.system.app.modules.accountingdossier.domain.request.AccountingDossierSubmitRequest;
import vn.system.app.modules.accountingdossier.repository.AccountingApprovalInstanceRepository;
import vn.system.app.modules.accountingdossier.repository.AccountingApprovalWorkflowTemplateRepository;
import vn.system.app.modules.accountingdossier.repository.AccountingDossierApprovalStepRepository;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.repository.UserRepository;

@Service
public class ApprovalStepGenerationService {

    @Value("${accounting.approval.workflow-v2:true}")
    private boolean workflowV2Enabled = true;
    private static final long DEFAULT_SLA_HOURS = 24;
    private static final String ACCOUNTANT_APPROVAL_PERMISSION = "Phê duyệt bộ chứng từ kế toán - Kế toán";
    private static final String CHIEF_ACCOUNTANT_APPROVAL_PERMISSION = "Phê duyệt bộ chứng từ kế toán - Kế toán trưởng";

    private final AccountingDossierApprovalStepRepository approvalStepRepository;
    private final AccountingApprovalWorkflowTemplateRepository workflowTemplateRepository;
    private final AccountingApprovalInstanceRepository approvalInstanceRepository;
    private final UserRepository userRepository;
    private final ApproverResolutionService approverResolutionService;
    private final ObjectMapper objectMapper;
    private final DossierAuditService dossierAuditService;

    public ApprovalStepGenerationService(
            AccountingDossierApprovalStepRepository approvalStepRepository,
            AccountingApprovalWorkflowTemplateRepository workflowTemplateRepository,
            AccountingApprovalInstanceRepository approvalInstanceRepository,
            UserRepository userRepository,
            ApproverResolutionService approverResolutionService,
            ObjectMapper objectMapper,
            DossierAuditService dossierAuditService) {
        this.approvalStepRepository = approvalStepRepository;
        this.workflowTemplateRepository = workflowTemplateRepository;
        this.approvalInstanceRepository = approvalInstanceRepository;
        this.userRepository = userRepository;
        this.approverResolutionService = approverResolutionService;
        this.objectMapper = objectMapper;
        this.dossierAuditService = dossierAuditService;
    }

    @Transactional
    public void generateApprovalSteps(AccountingDossier dossier, AccountingDossierSubmitRequest req) {
        List<AccountingDossierApprovalStep> oldSteps = approvalStepRepository
                .findByDossierIdAndActiveTrue(dossier.getId());
        oldSteps.forEach(step -> {
            step.setActive(false);
        });
        if (!oldSteps.isEmpty()) approvalStepRepository.saveAll(oldSteps);

        String creatorEmail = SecurityUtil.getCurrentUserLogin().orElse("");
        User creator = userRepository.findByEmail(creatorEmail);
        if (creator == null) {
            throw new IdInvalidException("Không xác định được người lập hồ sơ");
        }

        Optional<AccountingApprovalWorkflowTemplate> activeTemplate = workflowV2Enabled
                ? resolveActiveTemplate(dossier)
                : Optional.empty();
        if (activeTemplate.isPresent()) {
            generateApprovalStepsFromTemplate(dossier, creator, activeTemplate.get(), req);
            writeSkippedManagerAuditLogs(dossier);
            return;
        }

        List<AccountingDossierApprovalStep> steps = new ArrayList<>();

        if (req != null && req.getCustomSteps() != null && !req.getCustomSteps().isEmpty()) {
            for (AccountingDossierSubmitRequest.CustomStep customStep : req.getCustomSteps()) {
                AccountingDossierApprovalStep step = new AccountingDossierApprovalStep();
                step.setDossier(dossier);
                step.setStepOrder(customStep.getStepOrder());
                step.setStepName(customStep.getStepName());
                step.setApproverType(ApproverType.fromString(customStep.getApproverType()));
                step.setApproverUserId(customStep.getApproverUserId());

                if (step.getApproverType() == ApproverType.DEPARTMENT_MANAGER && step.getApproverUserId() == null) {
                    String managerId = resolveRequesterManagerId(creator);
                    if (managerId != null) {
                        step.setApproverUserId(managerId);
                    } else if (dossier.getReturnCount() != null && dossier.getReturnCount() >= 3) {
                        throw new IdInvalidException(
                                "Hồ sơ đã bị hoàn trả 3 lần, bạn cần có Trưởng bộ phận trực tiếp được cấu hình trong hệ thống để xác nhận duyệt lần này");
                    }
                }

                if (step.getApproverUserId() == null) {
                    if (step.getApproverType() == ApproverType.CHIEF_ACCOUNTANT) {
                        step.setApproverUserId(approverResolutionService.resolveChiefAccountantUserId(dossier.getCompany().getId()));
                    }
                }
                steps.add(step);
            }
        } else if (dossier.getDossierCategory() != null && dossier.getDossierCategory().getApprovalStepsConfig() != null
                && !dossier.getDossierCategory().getApprovalStepsConfig().trim().isEmpty()) {
            try {
                List<Map<String, Object>> configSteps = objectMapper.readValue(
                        dossier.getDossierCategory().getApprovalStepsConfig(),
                        new TypeReference<List<Map<String, Object>>>() {
                        });
                for (Map<String, Object> cStep : configSteps) {
                    AccountingDossierApprovalStep step = new AccountingDossierApprovalStep();
                    step.setDossier(dossier);
                    step.setStepOrder(Integer.parseInt(cStep.get("stepOrder").toString()));
                    step.setStepName(cStep.get("stepName").toString());
                    step.setApproverType(cStep.get("approverType") == null ? null : ApproverType.fromString(cStep.get("approverType").toString()));
                    if (cStep.get("approverUserId") != null) {
                        step.setApproverUserId(cStep.get("approverUserId").toString());
                    }

                    if (step.getApproverType() == ApproverType.DEPARTMENT_MANAGER && step.getApproverUserId() == null) {
                        String managerId = resolveRequesterManagerId(creator);
                        if (managerId != null) {
                            step.setApproverUserId(managerId);
                        } else if (dossier.getReturnCount() != null && dossier.getReturnCount() >= 3) {
                            throw new IdInvalidException(
                                    "Hồ sơ đã bị hoàn trả 3 lần, bạn cần có Trưởng bộ phận trực tiếp được cấu hình trong hệ thống để xác nhận duyệt lần này");
                        }
                    }
                    if (step.getApproverUserId() == null) {
                        if (step.getApproverType() == ApproverType.CHIEF_ACCOUNTANT) {
                            step.setApproverUserId(approverResolutionService.resolveChiefAccountantUserId(dossier.getCompany().getId()));
                        }
                    }
                    steps.add(step);
                }
            } catch (Exception e) {
                throw new IdInvalidException("Cấu hình luồng duyệt của danh mục không hợp lệ (INVALID_WORKFLOW_CONFIG)");
            }
        }

        if (steps.isEmpty()) {
            AccountingDossierApprovalStep step1 = new AccountingDossierApprovalStep();
            step1.setDossier(dossier);
            step1.setStepOrder(1);
            step1.setStepName("Trưởng bộ phận duyệt");
            step1.setApproverType(ApproverType.DEPARTMENT_MANAGER);
            String managerId = resolveRequesterManagerId(creator);
            if (managerId != null) {
                step1.setApproverUserId(managerId);
            } else {
                if (dossier.getReturnCount() != null && dossier.getReturnCount() >= 3) {
                    throw new IdInvalidException(
                            "Hồ sơ đã bị hoàn trả 3 lần, bạn cần có Trưởng bộ phận trực tiếp được cấu hình trong hệ thống để xác nhận duyệt lần này");
                }
            }
            steps.add(step1);

            AccountingDossierApprovalStep step2 = new AccountingDossierApprovalStep();
            step2.setDossier(dossier);
            step2.setStepOrder(2);
            step2.setStepName("Kế toán kiểm tra");
            step2.setApproverType(ApproverType.ACCOUNTANT);
            steps.add(step2);

            AccountingDossierApprovalStep step3 = new AccountingDossierApprovalStep();
            step3.setDossier(dossier);
            step3.setStepOrder(3);
            step3.setStepName("Kế toán trưởng duyệt");
            step3.setApproverType(ApproverType.CHIEF_ACCOUNTANT);
            String chiefAccountantId = approverResolutionService.resolveChiefAccountantUserId(dossier.getCompany().getId());
            step3.setApproverUserId(chiefAccountantId);
            steps.add(step3);
        } else {
            if (dossier.getReturnCount() != null && dossier.getReturnCount() >= 3) {
                boolean hasManagerStep = steps.stream()
                        .anyMatch(
                                s -> s.getApproverType() == ApproverType.DEPARTMENT_MANAGER && s.getApproverUserId() != null);
                if (!hasManagerStep) {
                    AccountingDossierApprovalStep mgrStep = new AccountingDossierApprovalStep();
                    mgrStep.setDossier(dossier);
                    mgrStep.setStepOrder(0);
                    mgrStep.setStepName("Trưởng bộ phận duyệt (Bắt buộc do hoàn trả >= 3 lần)");
                    mgrStep.setApproverType(ApproverType.DEPARTMENT_MANAGER);
                    String managerId = resolveRequesterManagerId(creator);
                    if (managerId == null) {
                        throw new IdInvalidException(
                                "Hồ sơ đã bị hoàn trả 3 lần, bạn cần có Trưởng bộ phận trực tiếp được cấu hình trong hệ thống để xác nhận duyệt lần này");
                    }
                    mgrStep.setApproverUserId(managerId);
                    steps.add(mgrStep);
                }
            }
        }

        // --- RESOLVE & VALIDATE DIRECTOR ---
        List<User> directors = approverResolutionService.resolveAllDirectorUserIds(dossier.getCompany().getId());
        if (directors.isEmpty()) {
            throw new IdInvalidException("Vui lòng cấu hình Giám đốc hợp lệ cho công ty (DIRECTOR_NOT_CONFIGURED)");
        }
        if (directors.size() > 1) {
            throw new IdInvalidException("Phát hiện nhiều hơn 1 Giám đốc được cấu hình cho công ty (DIRECTOR_MAPPING_AMBIGUOUS)");
        }
        String directorUserId = directors.get(0).getId();

        // Chặn SELF_APPROVAL_BLOCKED
        if (creator.getId().equals(directorUserId)) {
            throw new IdInvalidException("Giám đốc không được tự lập và tự duyệt hồ sơ của mình (SELF_APPROVAL_BLOCKED)");
        }

        // Chặn SAME_USER_APPROVAL_BLOCKED
        String chiefAccountId = approverResolutionService.resolveChiefAccountantUserId(dossier.getCompany().getId());
        if (chiefAccountId != null && chiefAccountId.equals(directorUserId)) {
            throw new IdInvalidException("Kế toán trưởng và Giám đốc không được là cùng một người (SAME_USER_APPROVAL_BLOCKED)");
        }

        // Loại bỏ mọi bước DIRECTOR do client/config gửi lên (nếu có) để chống lách luật
        steps.removeIf(s -> s.getApproverType() == ApproverType.DIRECTOR);

        // Append DIRECTOR step at the very end
        AccountingDossierApprovalStep directorStep = new AccountingDossierApprovalStep();
        directorStep.setDossier(dossier);
        directorStep.setStepOrder(999); // Tạm thời để cuối
        directorStep.setStepName("Giám đốc phê duyệt");
        directorStep.setApproverType(ApproverType.DIRECTOR);
        directorStep.setApproverUserId(directorUserId);
        steps.add(directorStep);

        // --- SORT, RE-INDEX & SET STATUS ---
        steps.sort(Comparator.comparingInt(AccountingDossierApprovalStep::getStepOrder));
        boolean currentSet = false;
        for (int i = 0; i < steps.size(); i++) {
            AccountingDossierApprovalStep s = steps.get(i);
            s.setStepOrder(i + 1);
            if (s.getStatus() == ApprovalStepStatus.SKIPPED) {
                continue;
            }
            if (isSelfApprovalStep(s, creator)) {
                s.setStatus(ApprovalStepStatus.SKIPPED);
                continue;
            }
            if (s.getApproverType() == ApproverType.DEPARTMENT_MANAGER && s.getApproverUserId() == null) {
                s.setStatus(ApprovalStepStatus.SKIPPED);
            } else {
                if (!currentSet) {
                    s.setStatus(ApprovalStepStatus.CURRENT);
                    s.setDueAt(Instant.now().plusSeconds(DEFAULT_SLA_HOURS * 3600));
                    currentSet = true;
                } else {
                    s.setStatus(ApprovalStepStatus.PENDING);
                    s.setDueAt(null);
                }
            }
        }
        if (!currentSet && !steps.isEmpty()) {
            steps.get(steps.size() - 1).setStatus(ApprovalStepStatus.CURRENT);
            steps.get(steps.size() - 1).setDueAt(Instant.now().plusSeconds(DEFAULT_SLA_HOURS * 3600));
        }

        approvalStepRepository.saveAll(steps);
        writeSkippedManagerAuditLogs(dossier);
    }

    private void writeSkippedManagerAuditLogs(AccountingDossier dossier) {
        List<AccountingDossierApprovalStep> activeSteps = approvalStepRepository
                .findByDossierIdAndActiveTrue(dossier.getId());
        for (AccountingDossierApprovalStep step : activeSteps) {
            if (step.getApproverType() == ApproverType.DEPARTMENT_MANAGER 
                    && step.getApproverUserId() == null 
                    && step.getStatus() == ApprovalStepStatus.SKIPPED) {
                dossierAuditService.writeLog(
                        dossier, 
                        "SKIP_APPROVAL_STEP", 
                        "Bỏ qua bước Trưởng bộ phận: người lập không có quản lý trực tiếp", 
                        "APPROVAL_STEP", 
                        step.getId(), 
                        null, 
                        "SKIPPED", 
                        null, 
                        null
                );
            }
        }
    }

    private void generateApprovalStepsFromTemplate(
            AccountingDossier dossier,
            User creator,
            AccountingApprovalWorkflowTemplate template,
            AccountingDossierSubmitRequest req) {
        List<String> errors = validateTemplateShape(template);
        if (!errors.isEmpty()) {
            throw new IdInvalidException("Luồng duyệt chưa hợp lệ: " + String.join("; ", errors));
        }

        List<AccountingDossierApprovalStep> steps = new ArrayList<>();
        for (AccountingApprovalWorkflowStep templateStep : sortedTemplateSteps(template)) {
            AccountingDossierApprovalStep step = buildRuntimeStepFromTemplate(dossier, creator, templateStep, req);
            steps.add(step);
        }

        validateDirectorRules(dossier, creator, steps);
        applyReturnCountManagerRule(dossier, creator, steps);
        activateFirstActionableStep(steps, creator);

        AccountingApprovalInstance instance = createApprovalInstance(dossier, template, steps);
        for (AccountingDossierApprovalStep step : steps) {
            step.setInstanceId(instance.getId());
        }
        approvalStepRepository.saveAll(steps);
    }

    private AccountingDossierApprovalStep buildRuntimeStepFromTemplate(
            AccountingDossier dossier,
            User creator,
            AccountingApprovalWorkflowStep templateStep,
            AccountingDossierSubmitRequest req) {
        AccountingDossierApprovalStep step = new AccountingDossierApprovalStep();
        step.setDossier(dossier);
        step.setStepKey(templateStep.getStepKey());
        step.setStepOrder(templateStep.getStepOrder());
        step.setStepName(templateStep.getStepName());
        step.setApproverType(toApproverType(templateStep));
        step.setSlaMinutes(templateStep.getSlaMinutes());
        step.setAllowDelegation(templateStep.isAllowDelegation());

        if (templateStep.getApproverStrategy() == ApproverStrategy.REQUESTER_MANAGER) {
            String managerId = resolveRequesterManagerId(creator);
            if (managerId != null) {
                step.setApproverUserId(managerId);
            } else if (templateStep.isRequired()) {
                step.setStatus(ApprovalStepStatus.SKIPPED);
            } else {
                step.setStatus(ApprovalStepStatus.SKIPPED);
            }
        } else if (templateStep.getApproverStrategy() == ApproverStrategy.COMPANY_DIRECTOR) {
            List<User> directors = approverResolutionService.resolveAllDirectorUserIds(dossier.getCompany().getId());
            if (directors.isEmpty()) {
                throw new IdInvalidException("Vui lòng cấu hình Giám đốc hợp lệ cho công ty (DIRECTOR_NOT_CONFIGURED)");
            }
            if (directors.size() > 1) {
                throw new IdInvalidException("Phát hiện nhiều hơn 1 Giám đốc được cấu hình cho công ty (DIRECTOR_MAPPING_AMBIGUOUS)");
            }
            step.setApproverUserId(directors.get(0).getId());
        } else if (templateStep.getApproverStrategy() == ApproverStrategy.COMPANY_ROLE) {
            List<User> users = new ArrayList<>(approverResolutionService.resolveBusinessApproversByPermission(
                    templateStep.getApproverRefId(),
                    dossier.getCompany().getId()));
            users.removeIf(u -> u.getId().equals(creator.getId()));
            if (users.isEmpty()) {
                if (templateStep.isRequired()) {
                    throw new IdInvalidException("Bước " + templateStep.getStepName()
                            + " không tìm thấy người có permission " + templateStep.getApproverRefId());
                } else {
                    step.setStatus(ApprovalStepStatus.SKIPPED);
                }
            } else if (users.size() == 1) {
                step.setApproverUserId(users.get(0).getId());
            } else {
                step.setEligibleApproverIds(users.stream().map(User::getId)
                        .collect(java.util.stream.Collectors.joining(",")));
                step.setApproverType(ApproverType.CUSTOM);
            }
        } else if (templateStep.getApproverStrategy() == ApproverStrategy.SPECIFIC_USER) {
            String userId = templateStep.getApproverRefId();
            if (userId == null || userId.trim().isEmpty() || userId.equals(creator.getId())) {
                if (templateStep.isRequired()) {
                    throw new IdInvalidException("Bước " + templateStep.getStepName() + " chưa cấu hình người duyệt hợp lệ (cấm tự duyệt)");
                } else {
                    step.setStatus(ApprovalStepStatus.SKIPPED);
                }
            } else {
                step.setApproverUserId(userId);
            }
        } else if (templateStep.getApproverStrategy() == ApproverStrategy.USER_SELECTABLE) {
            AccountingDossierSubmitRequest.CustomStep selected = findSelectedStep(req, templateStep.getStepKey(), templateStep.getStepOrder());
            if (selected == null || selected.getApproverUserId() == null || selected.getApproverUserId().trim().isEmpty() || selected.getApproverUserId().equals(creator.getId())) {
                if (templateStep.isRequired()) {
                    throw new IdInvalidException("Bước " + templateStep.getStepName() + " yêu cầu chọn người xử lý hợp lệ khi submit (cấm tự duyệt)");
                } else {
                    step.setStatus(ApprovalStepStatus.SKIPPED);
                }
            } else {
                step.setApproverUserId(selected.getApproverUserId());
                step.setApproverType(ApproverType.CUSTOM);
            }
        } else if (templateStep.getApproverStrategy() == ApproverStrategy.POSITION) {
            List<User> users = new ArrayList<>(approverResolutionService.resolvePositionApprovers(
                    templateStep.getPositionReferenceType(),
                    templateStep.getApproverRefId(),
                    templateStep.getPositionResolverScope(),
                    dossier.getCompany().getId(),
                    creator.getId(),
                    dossier.getDepartment() == null ? null : dossier.getDepartment().getId()));
            users.removeIf(u -> u.getId().equals(creator.getId()));
            if (users.isEmpty()) {
                if (templateStep.isRequired()) {
                    throw new IdInvalidException("Bước " + templateStep.getStepName()
                            + " không tìm thấy người phù hợp theo chức danh/cấp bậc");
                } else {
                    step.setStatus(ApprovalStepStatus.SKIPPED);
                }
            } else if (users.size() == 1) {
                step.setApproverUserId(users.get(0).getId());
            } else {
                step.setEligibleApproverIds(users.stream().map(User::getId)
                        .collect(java.util.stream.Collectors.joining(",")));
                step.setApproverType(ApproverType.CUSTOM);
            }
        } else {
            throw new IdInvalidException("Bước " + templateStep.getStepName()
                    + " dùng strategy chưa hỗ trợ khi submit: " + templateStep.getApproverStrategy());
        }

        return step;
    }

    private Optional<AccountingApprovalWorkflowTemplate> resolveActiveTemplate(AccountingDossier dossier) {
        Instant now = Instant.now();
        List<AccountingApprovalWorkflowTemplate> dbCandidates = workflowTemplateRepository.findActiveCandidatesWithScopes(
                WorkflowTemplateStatus.ACTIVE, dossier.getCompany().getId(), dossier.getDossierCategory() == null ? null : dossier.getDossierCategory().getId(), now);
        List<AccountingApprovalWorkflowTemplate> candidates = (dbCandidates == null || dbCandidates.isEmpty()
                ? workflowTemplateRepository.findByStatusOrderByPriorityAscIdAsc(WorkflowTemplateStatus.ACTIVE) : dbCandidates)
                .stream()
                .filter(template -> template.getCompanyId() == null || template.getCompanyId().equals(dossier.getCompany().getId()))
                .filter(template -> template.getDossierCategoryId() == null || (dossier.getDossierCategory() != null && template.getDossierCategoryId().equals(dossier.getDossierCategory().getId())))
                .filter(template -> template.getEffectiveFrom() == null || !template.getEffectiveFrom().isAfter(now))
                .filter(template -> template.getEffectiveTo() == null || template.getEffectiveTo().isAfter(now))
                .filter(template -> scopeMatches(template, dossier))
                .toList();
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        if (candidates.stream().anyMatch(template -> !template.isDefaultTemplate())) {
            candidates = candidates.stream().filter(template -> !template.isDefaultTemplate()).toList();
        }

        java.util.Map<AccountingApprovalWorkflowTemplate, Integer> matchScores = candidates.stream()
                .collect(java.util.stream.Collectors.toMap(template -> template, template -> matchScore(template, dossier)));
        int bestScore = matchScores.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        int bestPriority = candidates.stream()
                .filter(template -> matchScores.get(template) == bestScore)
                .map(AccountingApprovalWorkflowTemplate::getPriority)
                .min(Integer::compareTo)
                .orElse(100);
        List<AccountingApprovalWorkflowTemplate> best = candidates.stream()
                .filter(template -> matchScores.get(template) == bestScore)
                .filter(template -> template.getPriority().equals(bestPriority))
                .toList();
        if (best.size() > 1) {
            throw new IdInvalidException("Có nhiều luồng duyệt cùng khớp hồ sơ (WORKFLOW_AMBIGUOUS)");
        }
        return Optional.of(best.get(0));
    }

    private boolean scopeMatches(AccountingApprovalWorkflowTemplate template, AccountingDossier dossier) {
        if (template.getScopes().isEmpty()) {
            return true;
        }
        return template.getScopes().stream().anyMatch(scope -> {
            if (scope.getScopeType() == WorkflowScopeType.GLOBAL) {
                return true;
            }
            if (scope.getScopeType() == WorkflowScopeType.COMPANY) {
                return scope.getScopeId() == null || scope.getScopeId().equals(dossier.getCompany().getId());
            }
            if (scope.getScopeType() == WorkflowScopeType.DEPARTMENT) {
                return dossier.getDepartment() != null
                        && scope.getScopeId() != null
                        && scope.getScopeId().equals(dossier.getDepartment().getId());
            }
            return false;
        });
    }

    private int matchScore(AccountingApprovalWorkflowTemplate template, AccountingDossier dossier) {
        int score = 0;
        if (template.getCompanyId() != null) {
            score += 10;
        }
        if (template.getDossierCategoryId() != null) {
            score += 20;
        }
        if (template.getScopes().stream().anyMatch(scope -> scope.getScopeType() == WorkflowScopeType.DEPARTMENT
                && dossier.getDepartment() != null
                && scope.getScopeId() != null
                && scope.getScopeId().equals(dossier.getDepartment().getId()))) {
            score += 30;
        }
        return score;
    }

    private List<String> validateTemplateShape(AccountingApprovalWorkflowTemplate template) {
        List<String> errors = new ArrayList<>();
        List<AccountingApprovalWorkflowStep> steps = sortedTemplateSteps(template);
        if (steps.isEmpty()) {
            errors.add("Luồng duyệt cần ít nhất một bước");
            return errors;
        }
        for (AccountingApprovalWorkflowStep step : steps) {
            if (isBlank(step.getStepKey())) {
                errors.add("Có bước chưa có stepKey");
            }
            if (isBlank(step.getStepName())) {
                errors.add("Có bước chưa có tên hiển thị");
            }
            if (step.getStepOrder() == null || step.getStepOrder() <= 0) {
                errors.add("Có bước chưa có thứ tự hợp lệ");
            }
            if (step.getApproverStrategy() == null) {
                errors.add("Bước " + step.getStepName() + " chưa có chiến lược tìm người duyệt");
            }
            if (step.getApproverStrategy() == ApproverStrategy.COMPANY_ROLE && isBlank(step.getApproverRefId())) {
                errors.add("Bước " + step.getStepName() + " dùng COMPANY_ROLE nhưng chưa có tên permission");
            }
            if (step.getApproverStrategy() == ApproverStrategy.SPECIFIC_USER && isBlank(step.getApproverRefId())) {
                errors.add("Bước " + step.getStepName() + " dùng SPECIFIC_USER nhưng chưa có userId");
            }
            if (step.getApproverStrategy() == ApproverStrategy.POSITION
                    && (isBlank(step.getApproverRefId()) || step.getPositionReferenceType() == null
                            || step.getPositionResolverScope() == null)) {
                errors.add("Bước " + step.getStepName()
                        + " dùng POSITION nhưng chưa đủ loại tham chiếu, giá trị tham chiếu hoặc phạm vi tìm người");
            }
            if (step.getApproverStrategy() == ApproverStrategy.PROCESSING_GROUP) {
                errors.add("PROCESSING_GROUP thuộc Phase 5, chưa được publish trong Phase 2");
            }
        }
        long directorSteps = steps.stream()
                .filter(step -> step.getApproverStrategy() == ApproverStrategy.COMPANY_DIRECTOR)
                .count();
        if (directorSteps != 1) {
            errors.add("Luồng duyệt cần đúng một bước Giám đốc cuối");
        } else {
            AccountingApprovalWorkflowStep lastStep = steps.get(steps.size() - 1);
            if (lastStep.getApproverStrategy() != ApproverStrategy.COMPANY_DIRECTOR || !lastStep.isRequired()) {
                errors.add("Bước Giám đốc phải là bước bắt buộc cuối cùng");
            }
        }
        return errors;
    }

    private ApproverType toApproverType(AccountingApprovalWorkflowStep templateStep) {
        if (templateStep.getApproverStrategy() == ApproverStrategy.REQUESTER_MANAGER) {
            return ApproverType.DEPARTMENT_MANAGER;
        }
        if (templateStep.getApproverStrategy() == ApproverStrategy.COMPANY_DIRECTOR) {
            return ApproverType.DIRECTOR;
        }
        if (templateStep.getApproverStrategy() == ApproverStrategy.COMPANY_ROLE) {
            if (CHIEF_ACCOUNTANT_APPROVAL_PERMISSION.equals(templateStep.getApproverRefId())) {
                return ApproverType.CHIEF_ACCOUNTANT;
            }
            if (ACCOUNTANT_APPROVAL_PERMISSION.equals(templateStep.getApproverRefId())) {
                return ApproverType.ACCOUNTANT;
            }
            return ApproverType.CUSTOM;
        }
        return ApproverType.CUSTOM;
    }

    private void validateDirectorRules(AccountingDossier dossier, User creator, List<AccountingDossierApprovalStep> steps) {
        List<AccountingDossierApprovalStep> directorSteps = steps.stream()
                .filter(step -> step.getApproverType() == ApproverType.DIRECTOR)
                .toList();
        if (directorSteps.size() != 1) {
            throw new IdInvalidException("Luồng duyệt cần đúng một bước Giám đốc cuối");
        }
        AccountingDossierApprovalStep directorStep = directorSteps.get(0);
        if (creator.getId().equals(directorStep.getApproverUserId())) {
            throw new IdInvalidException("Giám đốc không được tự lập và tự duyệt hồ sơ của mình (SELF_APPROVAL_BLOCKED)");
        }
        String chiefAccountId = approverResolutionService.resolveChiefAccountantUserId(dossier.getCompany().getId());
        if (chiefAccountId != null && chiefAccountId.equals(directorStep.getApproverUserId())) {
            throw new IdInvalidException("Kế toán trưởng và Giám đốc không được là cùng một người (SAME_USER_APPROVAL_BLOCKED)");
        }
    }

    private void applyReturnCountManagerRule(AccountingDossier dossier, User creator, List<AccountingDossierApprovalStep> steps) {
        if (dossier.getReturnCount() == null || dossier.getReturnCount() < 3) {
            return;
        }
        boolean hasManagerStep = steps.stream()
                .anyMatch(s -> s.getApproverType() == ApproverType.DEPARTMENT_MANAGER && s.getApproverUserId() != null);
        if (hasManagerStep) {
            return;
        }
        String managerId = resolveRequesterManagerId(creator);
        if (managerId == null) {
            throw new IdInvalidException(
                    "Hồ sơ đã bị hoàn trả 3 lần, bạn cần có Trưởng bộ phận trực tiếp được cấu hình trong hệ thống để xác nhận duyệt lần này");
        }
        AccountingDossierApprovalStep mgrStep = new AccountingDossierApprovalStep();
        mgrStep.setDossier(dossier);
        mgrStep.setStepKey("RETURN_COUNT_MANAGER_REVIEW");
        mgrStep.setStepOrder(0);
        mgrStep.setStepName("Trưởng bộ phận duyệt (Bắt buộc do hoàn trả >= 3 lần)");
        mgrStep.setApproverType(ApproverType.DEPARTMENT_MANAGER);
        mgrStep.setApproverUserId(managerId);
        steps.add(mgrStep);
    }

    private void activateFirstActionableStep(List<AccountingDossierApprovalStep> steps, User creator) {
        steps.sort(Comparator.comparingInt(AccountingDossierApprovalStep::getStepOrder));
        boolean currentSet = false;
        for (int i = 0; i < steps.size(); i++) {
            AccountingDossierApprovalStep step = steps.get(i);
            step.setStepOrder(i + 1);
            if (step.getStatus() == ApprovalStepStatus.SKIPPED) {
                continue;
            }
            if (isSelfApprovalStep(step, creator)) {
                step.setStatus(ApprovalStepStatus.SKIPPED);
                continue;
            }
            if (step.getApproverType() == ApproverType.DEPARTMENT_MANAGER && step.getApproverUserId() == null) {
                step.setStatus(ApprovalStepStatus.SKIPPED);
                continue;
            }
            if (!currentSet) {
                step.setStatus(ApprovalStepStatus.CURRENT);
                step.setDueAt(calculateDueAt(step));
                currentSet = true;
            } else {
                step.setStatus(ApprovalStepStatus.PENDING);
                step.setDueAt(null);
            }
        }
        if (!currentSet && !steps.isEmpty()) {
            steps.get(steps.size() - 1).setStatus(ApprovalStepStatus.CURRENT);
            steps.get(steps.size() - 1).setDueAt(calculateDueAt(steps.get(steps.size() - 1)));
        }
    }

    private String resolveRequesterManagerId(User creator) {
        if (creator == null || creator.getDirectManager() == null) {
            return null;
        }
        String managerId = creator.getDirectManager().getId();
        return managerId != null && !managerId.equals(creator.getId()) ? managerId : null;
    }

    private boolean isSelfApprovalStep(AccountingDossierApprovalStep step, User creator) {
        return creator != null
                && step.getApproverType() != ApproverType.DIRECTOR
                && step.getApproverUserId() != null
                && step.getApproverUserId().equals(creator.getId());
    }

    private Instant calculateDueAt(AccountingDossierApprovalStep step) {
        int slaMinutes = step.getSlaMinutes() != null && step.getSlaMinutes() > 0
                ? step.getSlaMinutes()
                : (int) (DEFAULT_SLA_HOURS * 60);
        return Instant.now().plusSeconds(slaMinutes * 60L);
    }

    @Transactional
    public void completeActiveInstance(Long dossierId, WorkflowInstanceStatus status) {
        approvalInstanceRepository.findFirstByDossierIdAndStatusOrderByIdDesc(dossierId, WorkflowInstanceStatus.ACTIVE)
                .ifPresent(instance -> {
                    instance.setStatus(status);
                    instance.setCompletedAt(Instant.now());
                    approvalInstanceRepository.save(instance);
                });
    }

    private AccountingApprovalInstance createApprovalInstance(
            AccountingDossier dossier,
            AccountingApprovalWorkflowTemplate template,
            List<AccountingDossierApprovalStep> steps) {
        approvalInstanceRepository.findFirstByDossierIdAndStatusOrderByIdDesc(dossier.getId(), WorkflowInstanceStatus.ACTIVE)
                .ifPresent(active -> {
                    active.setStatus(WorkflowInstanceStatus.CANCELLED);
                    active.setCompletedAt(Instant.now());
                    approvalInstanceRepository.save(active);
                });

        AccountingApprovalInstance instance = new AccountingApprovalInstance();
        instance.setDossier(dossier);
        instance.setSubmissionNo((int) approvalInstanceRepository.countByDossierId(dossier.getId()) + 1);
        instance.setTemplateId(template.getId());
        instance.setTemplateVersion(template.getVersion());
        instance.setStatus(WorkflowInstanceStatus.ACTIVE);
        instance.setSnapshotJson(buildSnapshotJson(dossier, template, steps));
        return approvalInstanceRepository.saveAndFlush(instance);
    }

    private String buildSnapshotJson(
            AccountingDossier dossier,
            AccountingApprovalWorkflowTemplate template,
            List<AccountingDossierApprovalStep> steps) {
        try {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("source", "WORKFLOW_TEMPLATE_V2");
            snapshot.put("dossierId", dossier.getId());
            snapshot.put("templateId", template.getId());
            snapshot.put("templateCode", template.getCode());
            snapshot.put("templateName", template.getName());
            snapshot.put("templateVersion", template.getVersion());
            snapshot.put("steps", steps.stream().map(step -> {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("stepKey", step.getStepKey());
                row.put("stepOrder", step.getStepOrder());
                row.put("stepName", step.getStepName());
                row.put("approverType", step.getApproverType() == null ? null : step.getApproverType().name());
                row.put("approverUserId", step.getApproverUserId());
                row.put("status", step.getStatus() == null ? null : step.getStatus().name());
                return row;
            }).toList());
            return objectMapper.writeValueAsString(snapshot);
        } catch (Exception e) {
            throw new IdInvalidException("Không tạo được snapshot luồng duyệt");
        }
    }

    private AccountingDossierSubmitRequest.CustomStep findSelectedStep(
            AccountingDossierSubmitRequest req,
            String stepKey,
            Integer stepOrder) {
        if (req == null || req.getCustomSteps() == null || req.getCustomSteps().isEmpty()) {
            return null;
        }
        return req.getCustomSteps().stream()
                .filter(step -> stepKey != null && stepKey.equals(step.getStepKey()))
                .findFirst()
                .orElseGet(() -> req.getCustomSteps().stream()
                        .filter(step -> stepOrder != null && step.getStepOrder() == stepOrder)
                        .findFirst()
                        .orElse(null));
    }

    private List<AccountingApprovalWorkflowStep> sortedTemplateSteps(AccountingApprovalWorkflowTemplate template) {
        return template.getSteps().stream()
                .sorted(Comparator.comparing(AccountingApprovalWorkflowStep::getStepOrder,
                        Comparator.nullsLast(Integer::compareTo)))
                .toList();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
