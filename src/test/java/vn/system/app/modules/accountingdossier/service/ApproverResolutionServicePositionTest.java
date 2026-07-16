package vn.system.app.modules.accountingdossier.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.system.app.modules.accountingdossier.domain.enums.PositionReferenceType;
import vn.system.app.modules.accountingdossier.domain.enums.PositionResolverScope;
import vn.system.app.modules.accountingdossier.domain.enums.ApproverStrategy;
import vn.system.app.modules.accountingdossier.domain.enums.ApprovalRule;
import vn.system.app.modules.accountingdossier.domain.enums.WorkflowTemplateStatus;
import vn.system.app.modules.accountingdossier.domain.enums.WorkflowScopeType;
import vn.system.app.modules.accountingdossier.domain.AccountingApprovalWorkflowStep;
import vn.system.app.modules.accountingdossier.domain.AccountingApprovalWorkflowTemplate;
import vn.system.app.modules.accountingdossier.domain.AccountingApprovalWorkflowScope;
import vn.system.app.modules.accountingdossier.domain.AccountingDossier;
import vn.system.app.modules.accountingdossier.domain.AccountingDossierApprovalStep;
import vn.system.app.modules.accountingdossier.domain.AccountingApprovalInstance;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingApprovalPreviewDTO;
import vn.system.app.modules.accountingdossier.domain.request.AccountingDossierSubmitRequest;
import vn.system.app.modules.accountingdossier.repository.AccountingApprovalWorkflowTemplateRepository;
import vn.system.app.modules.accountingdossier.repository.AccountingDossierRepository;
import vn.system.app.modules.accountingdossier.repository.AccountingDossierApprovalStepRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.system.app.modules.accountingdossier.repository.AccountingApprovalInstanceRepository;
import vn.system.app.modules.company.domain.Company;
import vn.system.app.modules.company.repository.CompanyRepository;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.department.repository.DepartmentRepository;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.repository.UserRepository;
import vn.system.app.modules.userposition.repository.UserPositionRepository;

@ExtendWith(MockitoExtension.class)
class ApproverResolutionServicePositionTest {
    @Mock private UserRepository userRepository;
    @Mock private UserPositionRepository userPositionRepository;
    @Mock private AccountingApprovalWorkflowTemplateRepository templateRepository;
    @Mock private AccountingDossierRepository dossierRepository;
    @Mock private AccountingDossierApprovalStepRepository approvalStepRepository;
    @Mock private AccountingApprovalInstanceRepository approvalInstanceRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private DepartmentRepository departmentRepository;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        vn.system.app.common.util.UserScopeContext.set(new vn.system.app.common.util.UserScopeContext.UserScope(
                "test-admin", java.util.Collections.emptySet(), java.util.Collections.emptySet(), true, true, false, false));
    }

    @AfterEach
    void tearDown() {
        vn.system.app.common.util.UserScopeContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void resolvesExactlyOneUserByJobTitleInCompany() {
        User approver = user("approver-1");
        when(userPositionRepository.findActiveUsersByPositionReference(
                "JOB_TITLE", 12L, "COMPANY", 1L, null)).thenReturn(List.of(approver));

        List<User> result = service().resolvePositionApprovers(PositionReferenceType.JOB_TITLE, "12",
                PositionResolverScope.COMPANY, 1L, "requester", 8L);

        assertEquals(List.of(approver), result);
    }

    @Test
    void returnsNoUserWhenPositionReferenceDoesNotMatchAnyone() {
        when(userPositionRepository.findActiveUsersByPositionReference(
                "POSITION_LEVEL", 7L, "APPLIED_DEPARTMENT", 1L, 8L)).thenReturn(List.of());

        assertTrue(service().resolvePositionApprovers(PositionReferenceType.POSITION_LEVEL, "7",
                PositionResolverScope.APPLIED_DEPARTMENT, 1L, "requester", 8L).isEmpty());
    }

    @Test
    void returnsAllMatchingUsersInDeterministicOrderForQueueClaim() {
        User later = user("z-user");
        User first = user("a-user");
        when(userPositionRepository.findActiveUsersByPositionReference(
                "JOB_TITLE", 12L, "COMPANY", 1L, null)).thenReturn(List.of(later, first));

        List<User> result = service().resolvePositionApprovers(PositionReferenceType.JOB_TITLE, "12",
                PositionResolverScope.COMPANY, 1L, "requester", 8L);

        assertEquals(List.of("a-user", "z-user"), result.stream().map(User::getId).toList());
    }

    @Test
    void previewAndSubmitResolveTheSameSinglePositionApprover() {
        User creator = user("creator");
        creator.setEmail("creator@example.test");
        User positionApprover = user("position-approver");
        User director = user("director");
        AccountingDossier dossier = dossier();
        AccountingApprovalWorkflowTemplate template = template();
        when(dossierRepository.findById(99L)).thenReturn(java.util.Optional.of(dossier));
        when(templateRepository.findByStatusOrderByPriorityAscIdAsc(WorkflowTemplateStatus.ACTIVE)).thenReturn(List.of(template));
        when(userRepository.findByEmail("creator@example.test")).thenReturn(creator);
        // ApprovalStepGenerationService resolves the submitter from SecurityUtil; no security
        // context is needed for this unit test, so its empty-login lookup maps to the creator.
        when(userRepository.findByEmail("")).thenReturn(creator);
        when(userPositionRepository.findActiveUsersByPositionReference(
                "JOB_TITLE", 12L, "COMPANY", 1L, null)).thenReturn(List.of(positionApprover));
        when(userPositionRepository.findActiveUsersByPositionReference(
                "JOB_TITLE", 12L, "COMPANY", 1L,  null)).thenReturn(List.of(positionApprover));
        when(userRepository.findUsersByPermissionAndCompany(anyList(), anyList(), anyBoolean())).thenAnswer(invocation -> {
            List<String> permissions = invocation.getArgument(0);
            return permissions.contains("Phê duyệt bộ chứng từ kế toán - Giám đốc") ? List.of(director) : List.of();
        });

        ApproverResolutionService resolver = service();
        AccountingApprovalWorkflowService workflowService = new AccountingApprovalWorkflowService(
                templateRepository, dossierRepository, userRepository, resolver, companyRepository, departmentRepository, userPositionRepository);
        ResAccountingApprovalPreviewDTO preview = workflowService.preview(99L);

        when(approvalStepRepository.findByDossierIdAndActiveTrue(99L)).thenReturn(List.of());
        when(approvalInstanceRepository.countByDossierId(99L)).thenReturn(0L);
        when(approvalInstanceRepository.saveAndFlush(any(AccountingApprovalInstance.class))).thenAnswer(call -> {
            AccountingApprovalInstance instance = call.getArgument(0);
            instance.setId(1L);
            return instance;
        });
        ApprovalStepGenerationService generationService = new ApprovalStepGenerationService(
                approvalStepRepository, templateRepository, approvalInstanceRepository, userRepository, resolver,
                new com.fasterxml.jackson.databind.ObjectMapper(),
                org.mockito.Mockito.mock(DossierAuditService.class));
        generationService.generateApprovalSteps(dossier, new AccountingDossierSubmitRequest());

        assertEquals("position-approver", preview.getSteps().get(0).getApproverUserId());
        org.mockito.Mockito.verify(approvalStepRepository).saveAll(org.mockito.ArgumentMatchers.argThat(steps ->
                ((List<AccountingDossierApprovalStep>) steps).get(0).getApproverUserId().equals("position-approver")));
    }

    @Test
    void previewPrefersDepartmentWorkflowOverDefaultRegardlessOfPriority() {
        User creator = user("creator");
        creator.setEmail("creator@example.test");
        when(userRepository.findByEmail("creator@example.test")).thenReturn(creator);

        AccountingDossier dossier = dossier();
        AccountingApprovalWorkflowTemplate fallback = directorOnlyTemplate(10L, "DEFAULT", 1, true);
        AccountingApprovalWorkflowTemplate departmentSpecific = directorOnlyTemplate(20L, "DEPARTMENT", 999, false);
        addScope(fallback, WorkflowScopeType.COMPANY, 1L);
        addScope(departmentSpecific, WorkflowScopeType.DEPARTMENT, 8L);

        when(dossierRepository.findById(99L)).thenReturn(java.util.Optional.of(dossier));
        when(templateRepository.findByStatusOrderByPriorityAscIdAsc(WorkflowTemplateStatus.ACTIVE))
                .thenReturn(List.of(fallback, departmentSpecific));

        ResAccountingApprovalPreviewDTO preview = new AccountingApprovalWorkflowService(
                templateRepository, dossierRepository, userRepository, service(), companyRepository, departmentRepository, userPositionRepository).preview(99L);

        assertEquals(20L, preview.getTemplateId());
    }

    @Test
    void submitPrefersDepartmentWorkflowOverDefaultRegardlessOfPriority() {
        User creator = user("creator");
        creator.setEmail("creator@example.test");
        User director = user("director");
        AccountingDossier dossier = dossier();
        AccountingApprovalWorkflowTemplate fallback = directorOnlyTemplate(10L, "DEFAULT", 1, true);
        AccountingApprovalWorkflowTemplate departmentSpecific = directorOnlyTemplate(20L, "DEPARTMENT", 999, false);
        addScope(fallback, WorkflowScopeType.COMPANY, 1L);
        addScope(departmentSpecific, WorkflowScopeType.DEPARTMENT, 8L);

        when(templateRepository.findByStatusOrderByPriorityAscIdAsc(WorkflowTemplateStatus.ACTIVE))
                .thenReturn(List.of(fallback, departmentSpecific));
        when(userRepository.findByEmail("")).thenReturn(creator);
        when(userRepository.findUsersByPermissionAndCompany(anyList(), anyList(), anyBoolean())).thenAnswer(invocation -> {
            List<String> permissions = invocation.getArgument(0);
            return permissions.contains("Phê duyệt bộ chứng từ kế toán - Giám đốc") ? List.of(director) : List.of();
        });

        when(approvalStepRepository.findByDossierIdAndActiveTrue(99L)).thenReturn(List.of());
        when(approvalInstanceRepository.countByDossierId(99L)).thenReturn(0L);
        when(approvalInstanceRepository.saveAndFlush(any(AccountingApprovalInstance.class))).thenAnswer(call -> {
            AccountingApprovalInstance instance = call.getArgument(0);
            instance.setId(1L);
            return instance;
        });

        ApprovalStepGenerationService generationService = new ApprovalStepGenerationService(
                approvalStepRepository, templateRepository, approvalInstanceRepository, userRepository, service(),
                new com.fasterxml.jackson.databind.ObjectMapper(),
                org.mockito.Mockito.mock(DossierAuditService.class));

        generationService.generateApprovalSteps(dossier, new AccountingDossierSubmitRequest());

        org.mockito.Mockito.verify(approvalInstanceRepository).saveAndFlush(org.mockito.ArgumentMatchers.argThat(instance ->
                instance.getTemplateId().equals(20L)));
    }

    private ApproverResolutionService service() {
        return new ApproverResolutionService(userRepository, userPositionRepository);
    }

    private User user(String id) {
        User user = new User();
        user.setId(id);
        user.setEmail(id + "@example.test");
        return user;
    }

    private AccountingDossier dossier() {
        Company company = new Company(); company.setId(1L);
        Department department = new Department(); department.setId(8L); department.setCompany(company);
        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(99L); dossier.setCompany(company); dossier.setDepartment(department);
        dossier.setCreatedBy("creator@example.test");
        return dossier;
    }

    private AccountingApprovalWorkflowTemplate template() {
        AccountingApprovalWorkflowTemplate template = new AccountingApprovalWorkflowTemplate();
        template.setId(5L); template.setCompanyId(1L); template.setCode("POSITION-WF");
        template.setName("Position workflow"); template.setPriority(1); template.setVersion(1);
        template.setStatus(WorkflowTemplateStatus.ACTIVE);
        template.getSteps().add(step(template, 1, "Position", ApproverStrategy.POSITION, "12"));
        template.getSteps().get(0).setPositionReferenceType(PositionReferenceType.JOB_TITLE);
        template.getSteps().get(0).setPositionResolverScope(PositionResolverScope.COMPANY);
        template.getSteps().add(step(template, 2, "Director", ApproverStrategy.COMPANY_DIRECTOR, null));
        return template;
    }

    private AccountingApprovalWorkflowTemplate directorOnlyTemplate(
            Long id, String code, int priority, boolean defaultTemplate) {
        AccountingApprovalWorkflowTemplate template = new AccountingApprovalWorkflowTemplate();
        template.setId(id); template.setCompanyId(1L); template.setCode(code); template.setName(code);
        template.setPriority(priority); template.setDefaultTemplate(defaultTemplate); template.setVersion(1);
        template.setStatus(WorkflowTemplateStatus.ACTIVE);
        template.getSteps().add(step(template, 1, "Director", ApproverStrategy.COMPANY_DIRECTOR, null));
        return template;
    }

    private void addScope(AccountingApprovalWorkflowTemplate template, WorkflowScopeType type, Long scopeId) {
        AccountingApprovalWorkflowScope scope = new AccountingApprovalWorkflowScope();
        scope.setTemplate(template); scope.setScopeType(type); scope.setScopeId(scopeId);
        template.getScopes().add(scope);
    }

    private AccountingApprovalWorkflowStep step(AccountingApprovalWorkflowTemplate template, int order,
            String name, ApproverStrategy strategy, String referenceId) {
        AccountingApprovalWorkflowStep step = new AccountingApprovalWorkflowStep();
        step.setTemplate(template); step.setStepKey(name.toUpperCase()); step.setStepOrder(order); step.setStepName(name);
        step.setApproverStrategy(strategy); step.setApproverRefId(referenceId); step.setApprovalRule(ApprovalRule.ANY_ONE);
        step.setRequired(true);
        return step;
    }
}
