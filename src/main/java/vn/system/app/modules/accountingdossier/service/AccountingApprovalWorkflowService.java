package vn.system.app.modules.accountingdossier.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.common.util.UserScopeContext;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.common.util.error.PermissionException;
import vn.system.app.modules.accountingdossier.domain.AccountingApprovalWorkflowScope;
import vn.system.app.modules.accountingdossier.domain.AccountingApprovalWorkflowStep;
import vn.system.app.modules.accountingdossier.domain.AccountingApprovalWorkflowTemplate;
import vn.system.app.modules.accountingdossier.domain.AccountingDossier;
import vn.system.app.modules.accountingdossier.domain.enums.ApprovalRule;
import vn.system.app.modules.accountingdossier.domain.enums.ApproverStrategy;
import vn.system.app.modules.accountingdossier.domain.enums.WorkflowScopeType;
import vn.system.app.modules.accountingdossier.domain.enums.WorkflowTemplateStatus;
import vn.system.app.modules.accountingdossier.domain.request.AccountingApprovalWorkflowTemplateRequest;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingApprovalPreviewDTO;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingApprovalWorkflowTemplateDTO;
import vn.system.app.modules.accountingdossier.repository.AccountingApprovalWorkflowTemplateRepository;
import vn.system.app.modules.accountingdossier.repository.AccountingDossierRepository;
import vn.system.app.modules.company.repository.CompanyRepository;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.department.repository.DepartmentRepository;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.repository.UserRepository;
import vn.system.app.modules.userposition.domain.UserPosition;
import vn.system.app.modules.userposition.repository.UserPositionRepository;

@Service
public class AccountingApprovalWorkflowService {

    private static final String CHIEF_ACCOUNTANT_APPROVAL_PERMISSION = "Phê duyệt bộ chứng từ kế toán - Kế toán trưởng";

    @Value("${accounting.approval.workflow-v2:true}")
    private boolean workflowV2Enabled = true;

    private final AccountingApprovalWorkflowTemplateRepository templateRepository;
    private final AccountingDossierRepository dossierRepository;
    private final UserRepository userRepository;
    private final ApproverResolutionService approverResolutionService;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final UserPositionRepository userPositionRepository;

    public AccountingApprovalWorkflowService(
            AccountingApprovalWorkflowTemplateRepository templateRepository,
            AccountingDossierRepository dossierRepository,
            UserRepository userRepository,
            ApproverResolutionService approverResolutionService,
            CompanyRepository companyRepository,
            DepartmentRepository departmentRepository,
            UserPositionRepository userPositionRepository) {
        this.templateRepository = templateRepository;
        this.dossierRepository = dossierRepository;
        this.userRepository = userRepository;
        this.approverResolutionService = approverResolutionService;
        this.companyRepository = companyRepository;
        this.departmentRepository = departmentRepository;
        this.userPositionRepository = userPositionRepository;
    }

    @Transactional(readOnly = true)
    public List<ResAccountingApprovalWorkflowTemplateDTO> list() {
        UserScopeContext.UserScope scope = requireScope();
        return templateRepository.findAll().stream()
                .filter(template -> canAccessCompany(scope, template.getCompanyId()))
                .sorted(Comparator.comparing(AccountingApprovalWorkflowTemplate::getId).reversed())
                .map(this::toTemplateDTO)
                .toList();
    }

    @Transactional
    public ResAccountingApprovalWorkflowTemplateDTO create(AccountingApprovalWorkflowTemplateRequest req) {
        authorizeConfiguration(req.getCompanyId());
        if (templateRepository.existsByCodeAndCompanyIdAndVersion(req.getCode(), req.getCompanyId(), 1)) {
            throw new IdInvalidException("Mã luồng duyệt đã tồn tại trong công ty và phiên bản này");
        }
        AccountingApprovalWorkflowTemplate template = new AccountingApprovalWorkflowTemplate();
        applyRequest(template, req);
        template.setVersion(1);
        template.setStatus(WorkflowTemplateStatus.DRAFT);
        return toTemplateDTO(templateRepository.save(template));
    }

    @Transactional
    public ResAccountingApprovalWorkflowTemplateDTO updateDraft(Long id, AccountingApprovalWorkflowTemplateRequest req) {
        AccountingApprovalWorkflowTemplate template = fetchTemplate(id);
        authorizeConfiguration(template.getCompanyId());
        authorizeConfiguration(req.getCompanyId());
        if (template.getStatus() != WorkflowTemplateStatus.DRAFT) {
            throw new IdInvalidException("Chỉ được chỉnh sửa luồng ở trạng thái nháp");
        }
        if (templateRepository.existsByCodeAndCompanyIdAndVersionAndIdNot(
                req.getCode(), req.getCompanyId(), template.getVersion(), id)) {
            throw new IdInvalidException("Mã luồng duyệt đã tồn tại trong công ty và phiên bản này");
        }
        applyRequest(template, req);
        return toTemplateDTO(templateRepository.save(template));
    }

    @Transactional(readOnly = true)
    public List<String> validate(Long id) {
        AccountingApprovalWorkflowTemplate template = fetchTemplate(id);
        authorizeConfiguration(template.getCompanyId());
        return validateTemplate(template);
    }

    @Transactional
    public ResAccountingApprovalWorkflowTemplateDTO publish(Long id) {
        AccountingApprovalWorkflowTemplate template = fetchTemplate(id);
        authorizeConfiguration(template.getCompanyId());
        List<String> errors = validateTemplate(template);
        if (template.getEffectiveTo() != null && !template.getEffectiveTo().isAfter(Instant.now())) {
            errors.add("Thời điểm kết thúc hiệu lực phải ở tương lai trước khi kích hoạt");
        }
        if (!errors.isEmpty()) {
            throw new IdInvalidException("Luồng duyệt chưa hợp lệ: " + String.join("; ", errors));
        }
        assertNoAmbiguousActiveTemplate(template);
        template.setStatus(WorkflowTemplateStatus.ACTIVE);
        if (template.getEffectiveFrom() == null) {
            template.setEffectiveFrom(Instant.now());
        }
        return toTemplateDTO(templateRepository.save(template));
    }

    @Transactional
    public ResAccountingApprovalWorkflowTemplateDTO deactivate(Long id) {
        AccountingApprovalWorkflowTemplate template = fetchTemplate(id);
        authorizeConfiguration(template.getCompanyId());
        if (template.getStatus() != WorkflowTemplateStatus.ACTIVE) {
            throw new IdInvalidException("Chỉ có thể ngưng một luồng đang áp dụng");
        }
        template.setStatus(WorkflowTemplateStatus.INACTIVE);
        template.setEffectiveTo(Instant.now());
        return toTemplateDTO(templateRepository.save(template));
    }

    @Transactional
    public ResAccountingApprovalWorkflowTemplateDTO reactivate(Long id) {
        AccountingApprovalWorkflowTemplate template = fetchTemplate(id);
        authorizeConfiguration(template.getCompanyId());
        if (template.getStatus() != WorkflowTemplateStatus.INACTIVE) {
            throw new IdInvalidException("Chỉ có thể kích hoạt lại luồng đã ngưng áp dụng");
        }
        List<String> errors = validateTemplate(template);
        if (!errors.isEmpty()) {
            throw new IdInvalidException("Luồng duyệt chưa hợp lệ: " + String.join("; ", errors));
        }
        template.setEffectiveFrom(Instant.now());
        template.setEffectiveTo(null);
        assertNoAmbiguousActiveTemplate(template);
        template.setStatus(WorkflowTemplateStatus.ACTIVE);
        return toTemplateDTO(templateRepository.save(template));
    }

    @Transactional
    public ResAccountingApprovalWorkflowTemplateDTO copyToDraft(Long id) {
        AccountingApprovalWorkflowTemplate source = fetchTemplate(id);
        authorizeConfiguration(source.getCompanyId());
        AccountingApprovalWorkflowTemplate copy = new AccountingApprovalWorkflowTemplate();
        copy.setCode(source.getCode());
        int nextVersion = nextVersion(source.getCode(), source.getCompanyId());
        copy.setName(source.getName() + " (phiên bản " + nextVersion + ")");
        copy.setCompanyId(source.getCompanyId());
        copy.setDossierCategoryId(source.getDossierCategoryId());
        copy.setBusinessType(source.getBusinessType());
        copy.setPriority(source.getPriority());
        copy.setDefaultTemplate(false);
        copy.setVersion(nextVersion);
        copy.setStatus(WorkflowTemplateStatus.DRAFT);

        for (AccountingApprovalWorkflowStep sourceStep : source.getSteps()) {
            AccountingApprovalWorkflowStep step = new AccountingApprovalWorkflowStep();
            step.setTemplate(copy);
            step.setStepKey(sourceStep.getStepKey());
            step.setStepOrder(sourceStep.getStepOrder());
            step.setStepName(sourceStep.getStepName());
            step.setApproverStrategy(sourceStep.getApproverStrategy());
            step.setApproverRefId(sourceStep.getApproverRefId());
            step.setPositionReferenceType(sourceStep.getPositionReferenceType());
            step.setPositionResolverScope(sourceStep.getPositionResolverScope());
            step.setApprovalRule(sourceStep.getApprovalRule());
            step.setMinimumApprovals(sourceStep.getMinimumApprovals());
            step.setRequired(sourceStep.isRequired());
            step.setSlaMinutes(sourceStep.getSlaMinutes());
            step.setAllowDelegation(sourceStep.isAllowDelegation());
            step.setAllowForward(sourceStep.isAllowForward());
            step.setAllowSameApproverCollapse(sourceStep.isAllowSameApproverCollapse());
            copy.getSteps().add(step);
        }
        for (AccountingApprovalWorkflowScope sourceScope : source.getScopes()) {
            AccountingApprovalWorkflowScope scope = new AccountingApprovalWorkflowScope();
            scope.setTemplate(copy);
            scope.setScopeType(sourceScope.getScopeType());
            scope.setScopeId(sourceScope.getScopeId());
            scope.setIncludeChildren(sourceScope.isIncludeChildren());
            copy.getScopes().add(scope);
        }
        return toTemplateDTO(templateRepository.save(copy));
    }

    private int nextVersion(String code, Long companyId) {
        AccountingApprovalWorkflowTemplate latest = templateRepository
                .findTopByCodeAndCompanyIdOrderByVersionDesc(code, companyId);
        return latest == null || latest.getVersion() == null ? 1 : latest.getVersion() + 1;
    }

    @Transactional(readOnly = true)
    public ResAccountingApprovalPreviewDTO preview(Long dossierId) {
        AccountingDossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new IdInvalidException("Bộ chứng từ kế toán không tồn tại"));
        Optional<AccountingApprovalWorkflowTemplate> matchedTemplate = workflowV2Enabled
                ? resolveTemplate(dossier)
                : Optional.empty();
        if (matchedTemplate.isEmpty()) {
            return previewLegacyPhase1(dossier);
        }
        return previewTemplate(dossier, matchedTemplate.get());
    }

    private void applyRequest(AccountingApprovalWorkflowTemplate template, AccountingApprovalWorkflowTemplateRequest req) {
        template.setCode(req.getCode());
        template.setName(req.getName());
        template.setCompanyId(req.getCompanyId());
        template.setDossierCategoryId(req.getDossierCategoryId());
        template.setBusinessType(req.getBusinessType());
        template.setPriority(req.getPriority() == null ? 100 : req.getPriority());
        template.setDefaultTemplate(req.isDefaultTemplate());
        template.setEffectiveFrom(req.getEffectiveFrom());
        template.setEffectiveTo(req.getEffectiveTo());

        template.getSteps().clear();
        for (AccountingApprovalWorkflowTemplateRequest.StepRequest stepReq : req.getSteps()) {
            AccountingApprovalWorkflowStep step = new AccountingApprovalWorkflowStep();
            step.setTemplate(template);
            step.setStepKey(stepReq.getStepKey());
            step.setStepOrder(stepReq.getStepOrder());
            step.setStepName(stepReq.getStepName());
            step.setApproverStrategy(stepReq.getApproverStrategy());
            step.setApproverRefId(stepReq.getApproverRefId());
            step.setPositionReferenceType(stepReq.getPositionReferenceType());
            step.setPositionResolverScope(stepReq.getPositionResolverScope());
            step.setApprovalRule(stepReq.getApprovalRule() == null ? ApprovalRule.ANY_ONE : stepReq.getApprovalRule());
            step.setMinimumApprovals(stepReq.getMinimumApprovals());
            step.setRequired(stepReq.isRequired());
            step.setSlaMinutes(stepReq.getSlaMinutes());
            step.setAllowDelegation(stepReq.isAllowDelegation());
            step.setAllowForward(stepReq.isAllowForward());
            step.setAllowSameApproverCollapse(stepReq.isAllowSameApproverCollapse());
            template.getSteps().add(step);
        }

        template.getScopes().clear();
        for (AccountingApprovalWorkflowTemplateRequest.ScopeRequest scopeReq : req.getScopes()) {
            AccountingApprovalWorkflowScope scope = new AccountingApprovalWorkflowScope();
            scope.setTemplate(template);
            scope.setScopeType(scopeReq.getScopeType());
            scope.setScopeId(scopeReq.getScopeId());
            scope.setIncludeChildren(scopeReq.isIncludeChildren());
            template.getScopes().add(scope);
        }
    }

    private List<String> validateTemplate(AccountingApprovalWorkflowTemplate template) {
        List<String> errors = new ArrayList<>();
        if (template.getCompanyId() == null || !companyRepository.existsById(template.getCompanyId())) {
            errors.add("Công ty áp dụng không hợp lệ");
        }
        if (isBlank(template.getCode()) || template.getCode().trim().length() > 80) {
            errors.add("Mã luồng phải có từ 1 đến 80 ký tự");
        }
        if (isBlank(template.getName()) || template.getName().trim().length() > 255) {
            errors.add("Tên luồng phải có từ 1 đến 255 ký tự");
        }
        if (template.getPriority() == null || template.getPriority() <= 0) {
            errors.add("Độ ưu tiên phải lớn hơn 0");
        }
        if (!isBlank(template.getBusinessType())) {
            errors.add("Điều kiện businessType chưa được hỗ trợ ở phiên bản hiện tại");
        }
        validateScopes(template, errors);
        if (template.getSteps().isEmpty()) {
            errors.add("Luồng duyệt cần ít nhất một bước");
            return errors;
        }
        List<AccountingApprovalWorkflowStep> steps = sortedSteps(template);
        if (template.getEffectiveFrom() != null && template.getEffectiveTo() != null
                && !template.getEffectiveTo().isAfter(template.getEffectiveFrom())) {
            errors.add("Thời điểm kết thúc hiệu lực phải sau thời điểm bắt đầu");
        }
        Set<Integer> stepOrders = new HashSet<>();
        Set<String> stepKeys = new HashSet<>();
        for (AccountingApprovalWorkflowStep step : steps) {
            if (isBlank(step.getStepKey())) {
                errors.add("Có bước chưa có stepKey");
            }
            if (isBlank(step.getStepName())) {
                errors.add("Có bước chưa có tên hiển thị");
            }
            if (step.getStepOrder() == null || step.getStepOrder() <= 0) {
                errors.add("Có bước chưa có thứ tự hợp lệ");
            } else if (!stepOrders.add(step.getStepOrder())) {
                errors.add("Thứ tự bước " + step.getStepOrder() + " đang bị trùng");
            }
            if (!isBlank(step.getStepKey()) && !stepKeys.add(step.getStepKey().trim())) {
                errors.add("Mã bước " + step.getStepKey() + " đang bị trùng");
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
            if (step.getApprovalRule() != null && step.getApprovalRule() != ApprovalRule.ANY_ONE) {
                errors.add("Bước " + step.getStepName() + " chỉ hỗ trợ quy tắc ANY_ONE ở phiên bản hiện tại");
            }
            if (step.getMinimumApprovals() != null) {
                errors.add("Bước " + step.getStepName() + " chưa hỗ trợ số lượng phê duyệt tối thiểu");
            }
            if (step.isAllowForward() || step.isAllowSameApproverCollapse()) {
                errors.add("Bước " + step.getStepName() + " đang bật tùy chọn chưa được hỗ trợ ở phiên bản hiện tại");
            }
            if (step.getApproverStrategy() == ApproverStrategy.COMPANY_DIRECTOR && step.isAllowDelegation()) {
                errors.add("Bước Giám đốc duyệt cuối không được cho phép ủy quyền");
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

    private void validateScopes(AccountingApprovalWorkflowTemplate template, List<String> errors) {
        if (template.getScopes().isEmpty()) {
            errors.add("Luồng duyệt phải có phạm vi công ty hoặc phòng ban");
            return;
        }
        Set<String> uniqueScopes = new HashSet<>();
        for (AccountingApprovalWorkflowScope scope : template.getScopes()) {
            if (scope.getScopeType() == null || scope.getScopeId() == null) {
                errors.add("Có phạm vi áp dụng chưa đầy đủ");
                continue;
            }
            if (!uniqueScopes.add(scope.getScopeType() + ":" + scope.getScopeId())) {
                errors.add("Phạm vi " + scope.getScopeType() + " đang bị trùng");
            }
            if (scope.getScopeType() == WorkflowScopeType.COMPANY) {
                if (!scope.getScopeId().equals(template.getCompanyId())) {
                    errors.add("Phạm vi công ty phải trùng với công ty áp dụng");
                }
            } else if (scope.getScopeType() == WorkflowScopeType.DEPARTMENT) {
                if (scope.isIncludeChildren()) {
                    errors.add("Phạm vi phòng ban chưa hỗ trợ áp dụng cho cấp dưới");
                }
                Department department = departmentRepository.findById(scope.getScopeId()).orElse(null);
                if (department == null || department.getCompany() == null
                        || !Objects.equals(department.getCompany().getId(), template.getCompanyId())) {
                    errors.add("Phòng ban trong phạm vi không thuộc công ty áp dụng");
                }
            } else {
                errors.add("Chỉ hỗ trợ phạm vi COMPANY hoặc DEPARTMENT");
            }
        }
    }

    /**
     * Reject configurations that would make runtime template resolution ambiguous.
     * A more-specific department scope is allowed to coexist with a company scope;
     * only templates that can have the same score and priority are rejected.
     */
    private void assertNoAmbiguousActiveTemplate(AccountingApprovalWorkflowTemplate template) {
        List<AccountingApprovalWorkflowTemplate> conflicts = templateRepository.findByStatusForUpdate(WorkflowTemplateStatus.ACTIVE).stream()
                .filter(active -> !Objects.equals(active.getId(), template.getId()))
                .filter(active -> Objects.equals(active.getCompanyId(), template.getCompanyId()))
                .filter(active -> Objects.equals(active.getDossierCategoryId(), template.getDossierCategoryId()))
                .filter(active -> Objects.equals(active.getPriority(), template.getPriority()))
                .filter(active -> effectiveWindowsOverlap(active, template))
                .filter(active -> scopesCanTie(active, template))
                .toList();
        if (!conflicts.isEmpty()) {
            String conflictingWorkflows = conflicts.stream()
                    .map(active -> active.getCode() + " — " + active.getName())
                    .collect(Collectors.joining("; "));
            throw new IdInvalidException("Không thể áp dụng vì đang trùng với luồng: " + conflictingWorkflows
                    + ". Hãy đổi độ ưu tiên, phạm vi hoặc khoảng thời gian hiệu lực rồi thử lại. (WORKFLOW_AMBIGUOUS)");
        }
    }

    private boolean effectiveWindowsOverlap(
            AccountingApprovalWorkflowTemplate first,
            AccountingApprovalWorkflowTemplate second) {
        return (first.getEffectiveTo() == null || second.getEffectiveFrom() == null
                    || first.getEffectiveTo().isAfter(second.getEffectiveFrom()))
                && (second.getEffectiveTo() == null || first.getEffectiveFrom() == null
                    || second.getEffectiveTo().isAfter(first.getEffectiveFrom()));
    }

    private boolean scopesCanTie(AccountingApprovalWorkflowTemplate first, AccountingApprovalWorkflowTemplate second) {
        Set<Long> firstDepartments = departmentScopeIds(first);
        Set<Long> secondDepartments = departmentScopeIds(second);
        boolean sharedDepartment = firstDepartments.stream().anyMatch(secondDepartments::contains);
        return sharedDepartment || (hasGeneralScope(first) && hasGeneralScope(second));
    }

    private Set<Long> departmentScopeIds(AccountingApprovalWorkflowTemplate template) {
        return template.getScopes().stream()
                .filter(scope -> scope.getScopeType() == WorkflowScopeType.DEPARTMENT)
                .map(AccountingApprovalWorkflowScope::getScopeId)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
    }

    private boolean hasGeneralScope(AccountingApprovalWorkflowTemplate template) {
        return template.getScopes().isEmpty() || template.getScopes().stream().anyMatch(scope ->
                scope.getScopeType() == WorkflowScopeType.GLOBAL || scope.getScopeType() == WorkflowScopeType.COMPANY);
    }

    private Optional<AccountingApprovalWorkflowTemplate> resolveTemplate(AccountingDossier dossier) {
        Instant now = Instant.now();
        List<AccountingApprovalWorkflowTemplate> candidates = templateRepository
                .findByStatusOrderByPriorityAscIdAsc(WorkflowTemplateStatus.ACTIVE)
                .stream()
                .filter(template -> template.getCompanyId() == null
                        || template.getCompanyId().equals(dossier.getCompany().getId()))
                .filter(template -> template.getDossierCategoryId() == null
                        || (dossier.getDossierCategory() != null
                                && template.getDossierCategoryId().equals(dossier.getDossierCategory().getId())))
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

    private ResAccountingApprovalPreviewDTO previewTemplate(
            AccountingDossier dossier,
            AccountingApprovalWorkflowTemplate template) {
        List<String> warnings = new ArrayList<>();
        List<String> errors = validateTemplate(template);
        User creator = resolveDossierCreator(dossier);
        List<ResAccountingApprovalPreviewDTO.StepPreviewDTO> previewSteps = new ArrayList<>();
        for (AccountingApprovalWorkflowStep step : sortedSteps(template)) {
            previewSteps.add(resolveStepPreview(dossier, step, warnings, errors));
        }
        return ResAccountingApprovalPreviewDTO.builder()
                .dossierId(dossier.getId())
                .source("WORKFLOW_TEMPLATE_V2")
                .templateId(template.getId())
                .templateCode(template.getCode())
                .templateName(template.getName())
                .templateVersion(template.getVersion())
                .valid(errors.isEmpty())
                .warnings(warnings)
                .blockingErrors(errors)
                .sender(toPersonPreview(creator))
                .steps(previewSteps)
                .build();
    }

    private ResAccountingApprovalPreviewDTO previewLegacyPhase1(AccountingDossier dossier) {
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        List<ResAccountingApprovalPreviewDTO.StepPreviewDTO> steps = new ArrayList<>();
        User creator = userRepository.findByEmail(dossier.getCreatedBy());
        if (creator == null) creator = resolveCurrentUser();
        String managerId = resolveRequesterManagerId(creator);
        String managerLabel = managerId == null ? null : displayUser(creator.getDirectManager());
        if (managerId == null && dossier.getReturnCount() != null && dossier.getReturnCount() >= 3) {
            errors.add("Hồ sơ hoàn trả từ 3 lần cần Trưởng bộ phận trực tiếp");
        }
        steps.add(stepPreview(1, "DEPARTMENT_MANAGER", "Trưởng bộ phận duyệt",
                ApproverStrategy.REQUESTER_MANAGER, ApprovalRule.ANY_ONE, managerId,
                managerId == null ? "Bỏ qua nếu không bắt buộc" : managerLabel, null, true));

        steps.add(stepPreview(2, "ACCOUNTANT", "Kế toán kiểm tra",
                ApproverStrategy.COMPANY_ROLE, ApprovalRule.ANY_ONE, null,
                "Người có quyền Phê duyệt bộ chứng từ kế toán - Kế toán", null, true));

        String chiefId = approverResolutionService.resolveChiefAccountantUserId(dossier.getCompany().getId());
        if (chiefId == null) {
            errors.add("Chưa cấu hình Kế toán trưởng hợp lệ");
        }
        steps.add(stepPreview(3, "CHIEF_ACCOUNTANT", "Kế toán trưởng duyệt",
                ApproverStrategy.COMPANY_ROLE, ApprovalRule.ANY_ONE, chiefId,
                chiefId == null ? "Chưa resolve được" : userRepository.findById(chiefId).map(this::displayUser).orElse(chiefId), null, true));

        List<User> directors = approverResolutionService.resolveAllDirectorUserIds(dossier.getCompany().getId());
        if (directors.isEmpty()) {
            errors.add("Chưa cấu hình Giám đốc hợp lệ cho công ty (DIRECTOR_NOT_CONFIGURED)");
        }
        if (directors.size() > 1) {
            errors.add("Có nhiều hơn một Giám đốc hợp lệ (DIRECTOR_MAPPING_AMBIGUOUS)");
        }
        String directorId = directors.size() == 1 ? directors.get(0).getId() : null;
        steps.add(stepPreview(4, "DIRECTOR", "Giám đốc phê duyệt",
                ApproverStrategy.COMPANY_DIRECTOR, ApprovalRule.ANY_ONE, directorId,
                directorId == null ? "Chưa resolve được" : displayUser(directors.get(0)), null, true));

        if (dossier.getDossierCategory() != null && !isBlank(dossier.getDossierCategory().getApprovalStepsConfig())) {
            warnings.add("Preview legacy đang tóm tắt luồng Phase 1; category JSON chi tiết sẽ được xử lý khi submit.");
        }

        return ResAccountingApprovalPreviewDTO.builder()
                .dossierId(dossier.getId())
                .source("LEGACY_PHASE1")
                .valid(errors.isEmpty())
                .warnings(warnings)
                .blockingErrors(errors)
                .sender(toPersonPreview(creator))
                .steps(steps)
                .build();
    }

    private ResAccountingApprovalPreviewDTO.StepPreviewDTO resolveStepPreview(
            AccountingDossier dossier,
            AccountingApprovalWorkflowStep step,
            List<String> warnings,
            List<String> errors) {
        String assigneeUserId = null;
        String label = null;
        if (step.getApproverStrategy() == ApproverStrategy.REQUESTER_MANAGER) {
            User creator = userRepository.findByEmail(dossier.getCreatedBy());
            if (creator == null) creator = resolveCurrentUser();
            assigneeUserId = resolveRequesterManagerId(creator);
            label = assigneeUserId == null
                    ? "Bỏ qua nếu người lập không có quản lý trực tiếp hợp lệ"
                    : displayUser(creator.getDirectManager());
        } else if (step.getApproverStrategy() == ApproverStrategy.COMPANY_DIRECTOR) {
            List<User> directors = approverResolutionService.resolveAllDirectorUserIds(dossier.getCompany().getId());
            if (directors.size() == 1) {
                assigneeUserId = directors.get(0).getId();
                label = displayUser(directors.get(0));
            } else if (directors.isEmpty()) {
                errors.add("Bước " + step.getStepName() + " không tìm thấy Giám đốc hợp lệ");
                label = "Chưa resolve được";
            } else {
                errors.add("Bước " + step.getStepName() + " có nhiều Giám đốc hợp lệ");
                label = "Nhiều Giám đốc";
            }
        } else if (step.getApproverStrategy() == ApproverStrategy.COMPANY_ROLE) {
            List<User> users = approverResolutionService.resolveBusinessApproversByPermission(
                    step.getApproverRefId(),
                    dossier.getCompany().getId());
            if (users.isEmpty()) {
                errors.add("Bước " + step.getStepName() + " không tìm thấy người có permission " + step.getApproverRefId());
                label = "Chưa resolve được";
            } else if (users.size() == 1) {
                assigneeUserId = users.get(0).getId();
                label = displayUser(users.get(0));
            } else {
                label = users.size() + " người có quyền xử lý";
                warnings.add("Bước " + step.getStepName() + " resolve ra nhiều người; runtime cần queue/claim hoặc primary approver.");
            }
        } else if (step.getApproverStrategy() == ApproverStrategy.SPECIFIC_USER) {
            assigneeUserId = step.getApproverRefId();
            label = userRepository.findById(step.getApproverRefId()).map(this::displayUser).orElse(step.getApproverRefId());
        } else if (step.getApproverStrategy() == ApproverStrategy.USER_SELECTABLE) {
            label = "Người lập sẽ chọn người duyệt khi gửi hồ sơ";
        } else if (step.getApproverStrategy() == ApproverStrategy.POSITION) {
            User creator = userRepository.findByEmail(dossier.getCreatedBy());
            List<User> users = approverResolutionService.resolvePositionApprovers(
                    step.getPositionReferenceType(), step.getApproverRefId(), step.getPositionResolverScope(),
                    dossier.getCompany().getId(), creator == null ? null : creator.getId(),
                    dossier.getDepartment() == null ? null : dossier.getDepartment().getId());
            if (users.isEmpty()) {
                errors.add("Bước " + step.getStepName() + " không tìm thấy người phù hợp theo chức danh/cấp bậc");
                label = "Chưa resolve được";
            } else if (users.size() == 1) {
                assigneeUserId = users.get(0).getId();
                label = displayUser(users.get(0));
            } else {
                label = users.size() + " người khớp chức danh/cấp bậc";
                warnings.add("Bước " + step.getStepName()
                        + " resolve ra nhiều người theo chức danh/cấp bậc; runtime sẽ tạo hàng đợi để một người claim.");
            }
        } else {
            errors.add("Bước " + step.getStepName() + " dùng strategy chưa hỗ trợ runtime trong Phase 2");
            label = "Chưa hỗ trợ";
        }

        return stepPreview(step.getStepOrder(), step.getStepKey(), step.getStepName(), step.getApproverStrategy(),
                step.getApprovalRule(), assigneeUserId, label, step.getSlaMinutes(), step.isRequired());
    }

    private User resolveDossierCreator(AccountingDossier dossier) {
        User creator = userRepository.findByEmail(dossier.getCreatedBy());
        if (creator != null) {
            return creator;
        }
        if (!isBlank(dossier.getCreatorId())) {
            return userRepository.findById(dossier.getCreatorId()).orElse(resolveCurrentUser());
        }
        return resolveCurrentUser();
    }

    private String resolveRequesterManagerId(User creator) {
        if (creator == null || creator.getDirectManager() == null) {
            return null;
        }
        String managerId = creator.getDirectManager().getId();
        return managerId != null && !managerId.equals(creator.getId()) ? managerId : null;
    }

    private ResAccountingApprovalPreviewDTO.StepPreviewDTO stepPreview(
            Integer order,
            String key,
            String name,
            ApproverStrategy strategy,
            ApprovalRule rule,
            String approverUserId,
            String assigneeLabel,
            Integer slaMinutes,
            boolean required) {
        return ResAccountingApprovalPreviewDTO.StepPreviewDTO.builder()
                .stepOrder(order)
                .stepKey(key)
                .stepName(name)
                .approverStrategy(strategy)
                .approvalRule(rule)
                .approverUserId(approverUserId)
                .assigneeLabel(assigneeLabel)
                .assignee(toPersonPreview(approverUserId))
                .slaMinutes(slaMinutes)
                .required(required)
                .build();
    }

    private ResAccountingApprovalPreviewDTO.PersonPreviewDTO toPersonPreview(String userId) {
        if (isBlank(userId)) {
            return null;
        }
        return userRepository.findById(userId).map(this::toPersonPreview).orElse(null);
    }

    private ResAccountingApprovalPreviewDTO.PersonPreviewDTO toPersonPreview(User user) {
        if (user == null) {
            return null;
        }
        ResAccountingApprovalPreviewDTO.PersonPreviewDTO.PersonPreviewDTOBuilder builder =
                ResAccountingApprovalPreviewDTO.PersonPreviewDTO.builder()
                        .userId(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .roleName(user.getRole() == null ? null : user.getRole().getName());

        List<UserPosition> positions = userPositionRepository == null
                ? List.of()
                : userPositionRepository.findActiveFullByUserId(user.getId());
        if (!positions.isEmpty()) {
            UserPosition position = positions.stream()
                    .filter(item -> "DEPARTMENT".equalsIgnoreCase(item.getSource()))
                    .findFirst()
                    .orElse(positions.get(0));
            applyPositionPreview(builder, position);
        }
        return builder.build();
    }

    private void applyPositionPreview(
            ResAccountingApprovalPreviewDTO.PersonPreviewDTO.PersonPreviewDTOBuilder builder,
            UserPosition position) {
        if (position.getDepartmentJobTitle() != null) {
            var departmentJobTitle = position.getDepartmentJobTitle();
            var jobTitle = departmentJobTitle.getJobTitle();
            var department = departmentJobTitle.getDepartment();
            builder.jobTitleName(jobTitle == null ? null : jobTitle.getNameVi());
            builder.positionLevelCode(jobTitle == null || jobTitle.getPositionLevel() == null ? null : jobTitle.getPositionLevel().getCode());
            builder.departmentName(department == null ? null : department.getName());
            builder.companyName(department == null || department.getCompany() == null ? null : department.getCompany().getName());
            return;
        }
        if (position.getSectionJobTitle() != null) {
            var sectionJobTitle = position.getSectionJobTitle();
            var jobTitle = sectionJobTitle.getJobTitle();
            var section = sectionJobTitle.getSection();
            var department = section == null ? null : section.getDepartment();
            builder.jobTitleName(jobTitle == null ? null : jobTitle.getNameVi());
            builder.positionLevelCode(jobTitle == null || jobTitle.getPositionLevel() == null ? null : jobTitle.getPositionLevel().getCode());
            builder.sectionName(section == null ? null : section.getName());
            builder.departmentName(department == null ? null : department.getName());
            builder.companyName(department == null || department.getCompany() == null ? null : department.getCompany().getName());
            return;
        }
        if (position.getCompanyJobTitle() != null) {
            var companyJobTitle = position.getCompanyJobTitle();
            var jobTitle = companyJobTitle.getJobTitle();
            var company = companyJobTitle.getCompany();
            builder.jobTitleName(jobTitle == null ? null : jobTitle.getNameVi());
            builder.positionLevelCode(jobTitle == null || jobTitle.getPositionLevel() == null ? null : jobTitle.getPositionLevel().getCode());
            builder.companyName(company == null ? null : company.getName());
        }
    }

    private String displayUser(User user) {
        if (user == null) {
            return "Chưa xác định";
        }
        if (!isBlank(user.getName()) && !isBlank(user.getEmail())) {
            return user.getName() + " (" + user.getEmail() + ")";
        }
        if (!isBlank(user.getName())) {
            return user.getName();
        }
        if (!isBlank(user.getEmail())) {
            return user.getEmail();
        }
        return user.getId();
    }

    private void authorizeConfiguration(Long companyId) {
        UserScopeContext.UserScope scope = requireScope();
        if (scope.isSuperAdmin() || scope.isAdminLevel()) {
            return;
        }
        if (scope.isCompanyLevel() && companyId != null && scope.companyIds().contains(companyId)) {
            return;
        }
        throw new PermissionException("Bạn không có quyền cấu hình luồng duyệt của công ty này");
    }

    private boolean canAccessCompany(UserScopeContext.UserScope scope, Long companyId) {
        return (scope.isSuperAdmin() || scope.isAdminLevel())
                || (scope.isCompanyLevel() && companyId != null && scope.companyIds().contains(companyId));
    }

    private UserScopeContext.UserScope requireScope() {
        UserScopeContext.UserScope scope = UserScopeContext.get();
        if (scope == null || scope.userId() == null) {
            throw new PermissionException("Không xác định được phạm vi quyền của người dùng");
        }
        return scope;
    }

    private AccountingApprovalWorkflowTemplate fetchTemplate(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Luồng duyệt không tồn tại"));
    }

    private User resolveCurrentUser() {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IdInvalidException("Không xác định được người dùng hiện tại");
        }
        return user;
    }

    private List<AccountingApprovalWorkflowStep> sortedSteps(AccountingApprovalWorkflowTemplate template) {
        return template.getSteps().stream()
                .sorted(Comparator.comparing(AccountingApprovalWorkflowStep::getStepOrder,
                        Comparator.nullsLast(Integer::compareTo)))
                .toList();
    }

    private ResAccountingApprovalWorkflowTemplateDTO toTemplateDTO(AccountingApprovalWorkflowTemplate template) {
        return ResAccountingApprovalWorkflowTemplateDTO.builder()
                .id(template.getId())
                .code(template.getCode())
                .name(template.getName())
                .companyId(template.getCompanyId())
                .dossierCategoryId(template.getDossierCategoryId())
                .businessType(template.getBusinessType())
                .priority(template.getPriority())
                .defaultTemplate(template.isDefaultTemplate())
                .status(template.getStatus())
                .version(template.getVersion())
                .effectiveFrom(template.getEffectiveFrom())
                .effectiveTo(template.getEffectiveTo())
                .steps(sortedSteps(template).stream().map(step -> ResAccountingApprovalWorkflowTemplateDTO.StepDTO.builder()
                        .id(step.getId())
                        .stepKey(step.getStepKey())
                        .stepOrder(step.getStepOrder())
                        .stepName(step.getStepName())
                        .approverStrategy(step.getApproverStrategy())
                        .approverRefId(step.getApproverRefId())
                        .positionReferenceType(step.getPositionReferenceType())
                        .positionResolverScope(step.getPositionResolverScope())
                        .approvalRule(step.getApprovalRule())
                        .minimumApprovals(step.getMinimumApprovals())
                        .required(step.isRequired())
                        .slaMinutes(step.getSlaMinutes())
                        .allowDelegation(step.isAllowDelegation())
                        .allowForward(step.isAllowForward())
                        .allowSameApproverCollapse(step.isAllowSameApproverCollapse())
                        .build()).toList())
                .scopes(template.getScopes().stream().map(scope -> ResAccountingApprovalWorkflowTemplateDTO.ScopeDTO.builder()
                        .id(scope.getId())
                        .scopeType(scope.getScopeType())
                        .scopeId(scope.getScopeId())
                        .includeChildren(scope.isIncludeChildren())
                        .build()).toList())
                .build();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
