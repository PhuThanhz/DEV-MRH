package vn.system.app.modules.accountingdossier.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.common.util.error.PermissionException;
import vn.system.app.common.util.error.DuplicateInvoiceWarningException;
import vn.system.app.common.config.AppProperties;
import vn.system.app.modules.accountingdossier.domain.AccountingDossier;
import vn.system.app.modules.accountingdossier.domain.AccountingDossierCategory;
import vn.system.app.modules.accountingdossier.domain.AccountingDossierCategoryDocument;
import vn.system.app.modules.accountingdossier.domain.AccountingDossierDocument;
import vn.system.app.modules.accountingdossier.domain.enums.AccountingDossierCategoryMode;
import vn.system.app.modules.accountingdossier.domain.enums.AccountingDossierStatus;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingDossierBulkActionDTO;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingDossierDTO;
import vn.system.app.modules.accountingdossier.domain.request.AccountingDossierSubmitRequest;
import vn.system.app.modules.accountingdossier.domain.request.AccountingDossierDocumentRequest;
import vn.system.app.modules.accountingdossier.domain.AccountingDossierApprovalStep;
import vn.system.app.modules.accountingdossier.repository.AccountingDossierAuditLogRepository;
import vn.system.app.modules.accountingdossier.repository.AccountingDossierCategoryRepository;
import vn.system.app.modules.accountingdossier.repository.AccountingDossierDocumentRepository;
import vn.system.app.modules.accountingdossier.repository.AccountingDossierDocumentVersionRepository;
import vn.system.app.modules.accountingdossier.repository.AccountingDossierApprovalStepRepository;
import vn.system.app.modules.accountingdossier.repository.AccountingDossierRepository;
import vn.system.app.modules.accountingdossier.repository.AccountingDossierSequenceRepository;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.company.domain.Company;
import vn.system.app.modules.company.repository.CompanyRepository;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.department.repository.DepartmentRepository;
import vn.system.app.modules.document.domain.AccountingDocumentCategory;
import vn.system.app.modules.document.repository.AccountingDocumentCategoryRepository;
import vn.system.app.modules.document.repository.DocumentRepository;
import vn.system.app.modules.section.repository.SectionRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountingDossierServiceTest {

    @Mock
    private AccountingDossierRepository repository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private AccountingDossierSequenceRepository sequenceRepository;
    @Mock
    private AccountingDossierAuditLogRepository auditLogRepository;
    @Mock
    private AccountingDossierDocumentRepository documentItemRepository;
    @Mock
    private AccountingDossierCategoryRepository dossierCategoryRepository;
    @Mock
    private AccountingDocumentCategoryRepository accountingCategoryRepository;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private AccountingDossierDocumentVersionRepository documentVersionRepository;
    @Mock
    private vn.system.app.modules.user.repository.UserRepository userRepository;
    @Mock
    private AccountingDossierApprovalStepRepository approvalStepRepository;
    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private AccountingDossierService service;

    private SecurityContext originalSecurityContext;

    @BeforeEach
    void setUp() {
        originalSecurityContext = SecurityContextHolder.getContext();
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("test-user");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(appProperties.getBaseUrl()).thenReturn("http://localhost:8080");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.setContext(originalSecurityContext);
    }

    @Test
    void testDeleteDossierSuccess() {
        Long dossierId = 1L;
        Company company = new Company();
        company.setId(10L);

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(dossierId);
        dossier.setStatus(AccountingDossierStatus.DRAFT);
        dossier.setActive(true);
        dossier.setCompany(company);

        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));
        when(documentItemRepository.findByDossierIdAndActiveTrue(dossierId)).thenReturn(Collections.emptyList());

        // We also need to stub user scopes check inside validateCompanyScope if any.
        // Let's check how validateCompanyScope is implemented in service.

        service.delete(dossierId);

        assertFalse(dossier.isActive());
        assertNotNull(dossier.getDeletedAt());
        assertEquals("test-user", dossier.getDeletedBy());
        verify(repository, times(1)).save(dossier);
    }

    @Test
    void testSubmitDossierMissingRequiredDocuments() {
        Long dossierId = 1L;
        Company company = new Company();
        company.setId(10L);

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(dossierId);
        dossier.setStatus(AccountingDossierStatus.DRAFT);
        dossier.setCategoryMode(AccountingDossierCategoryMode.TEMPLATE);
        dossier.setCompany(company);

        AccountingDossierCategory category = new AccountingDossierCategory();
        category.setId(5L);

        AccountingDocumentCategory docCategory = new AccountingDocumentCategory();
        docCategory.setId(20L);
        docCategory.setCategoryName("Hóa đơn");

        AccountingDossierCategoryDocument categoryDoc = new AccountingDossierCategoryDocument();
        categoryDoc.setDocumentCategory(docCategory);
        categoryDoc.setRequired(true);

        category.setCategoryDocuments(List.of(categoryDoc));
        dossier.setDossierCategory(category);

        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));
        when(documentItemRepository.countByDossierIdAndActiveTrue(dossierId)).thenReturn(1L);
        // Returns an empty list, so the required "Hóa đơn" document category is missing.
        when(documentItemRepository.findByDossierIdAndActiveTrue(dossierId)).thenReturn(Collections.emptyList());

        IdInvalidException exception = assertThrows(IdInvalidException.class, () -> {
            service.submit(dossierId);
        });

        assertTrue(exception.getMessage().contains("Thiếu chứng từ bắt buộc: Hóa đơn"));
    }

    @Test
    void testSubmitDossierRequiredDocumentWithoutAttachmentThrowsException() {
        Long dossierId = 1L;
        Company company = new Company();
        company.setId(10L);

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(dossierId);
        dossier.setStatus(AccountingDossierStatus.DRAFT);
        dossier.setCategoryMode(AccountingDossierCategoryMode.TEMPLATE);
        dossier.setCompany(company);

        AccountingDossierCategory category = new AccountingDossierCategory();
        category.setId(5L);

        AccountingDocumentCategory docCategory = new AccountingDocumentCategory();
        docCategory.setId(20L);
        docCategory.setCategoryName("Hóa đơn");

        AccountingDossierCategoryDocument categoryDoc = new AccountingDossierCategoryDocument();
        categoryDoc.setDocumentCategory(docCategory);
        categoryDoc.setRequired(true);

        AccountingDossierDocument placeholder = new AccountingDossierDocument();
        placeholder.setAccountingCategory(docCategory);
        placeholder.setDocumentName("Hóa đơn");

        category.setCategoryDocuments(List.of(categoryDoc));
        dossier.setDossierCategory(category);

        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));
        when(documentItemRepository.countByDossierIdAndActiveTrue(dossierId)).thenReturn(1L);
        when(documentItemRepository.findByDossierIdAndActiveTrue(dossierId)).thenReturn(List.of(placeholder));

        IdInvalidException exception = assertThrows(IdInvalidException.class, () -> service.submit(dossierId));

        assertTrue(exception.getMessage().contains("chưa có file/link đính kèm: Hóa đơn"));
    }

    @Test
    void testSubmitDossierGeneratesApprovalSteps() {
        Long dossierId = 1L;
        Company company = new Company();
        company.setId(10L);

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(dossierId);
        dossier.setStatus(AccountingDossierStatus.DRAFT);
        dossier.setCategoryMode(AccountingDossierCategoryMode.UNSTRUCTURED);
        dossier.setCompany(company);
        dossier.setDossierCode("BCT-2026-000001");

        User creator = new User();
        creator.setId("user-creator-id");
        creator.setEmail("test-user");
        
        User manager = new User();
        manager.setId("user-manager-id");
        manager.setEmail("manager-email");
        creator.setDirectManager(manager);

        User accountant = new User();
        accountant.setId("user-accountant-id");

        User chief = new User();
        chief.setId("user-chief-id");
        chief.setActive(true);
        vn.system.app.modules.role.domain.Role role = new vn.system.app.modules.role.domain.Role();
        role.setName("CHIEF_ACCOUNTANT");
        chief.setRole(role);

        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));
        when(documentItemRepository.countByDossierIdAndActiveTrue(dossierId)).thenReturn(1L);
        when(userRepository.findByEmail("test-user")).thenReturn(creator);
        when(approvalStepRepository.findByDossierIdAndActiveTrue(dossierId)).thenReturn(Collections.emptyList());
        when(userRepository.findUsersByPermissionAndCompany(anyList(), anyList(), anyBoolean())).thenReturn(List.of(accountant));
        when(userRepository.findAll()).thenReturn(List.of(chief));
        when(repository.save(any(AccountingDossier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResAccountingDossierDTO result = service.submit(dossierId);

        assertEquals(AccountingDossierStatus.SUBMITTED, result.getStatus());
        verify(approvalStepRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testSubmitDossierCreatesRoleBasedAccountantStep() {
        Long dossierId = 1L;
        Company company = new Company();
        company.setId(10L);

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(dossierId);
        dossier.setStatus(AccountingDossierStatus.DRAFT);
        dossier.setCategoryMode(AccountingDossierCategoryMode.UNSTRUCTURED);
        dossier.setCompany(company);
        dossier.setDossierCode("BCT-2026-000001");

        User creator = new User();
        creator.setId("user-creator-id");
        creator.setEmail("test-user");

        User chief = new User();
        chief.setId("user-chief-id");
        chief.setActive(true);
        vn.system.app.modules.role.domain.Role chiefRole = new vn.system.app.modules.role.domain.Role();
        chiefRole.setName("CHIEF_ACCOUNTANT");
        chief.setRole(chiefRole);

        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));
        when(documentItemRepository.countByDossierIdAndActiveTrue(dossierId)).thenReturn(1L);
        when(userRepository.findByEmail("test-user")).thenReturn(creator);
        when(approvalStepRepository.findByDossierIdAndActiveTrue(dossierId)).thenReturn(Collections.emptyList());
        when(userRepository.findAll()).thenReturn(List.of(chief));
        when(repository.save(any(AccountingDossier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.submit(dossierId);

        verify(approvalStepRepository).saveAll(argThat(steps -> {
            List<AccountingDossierApprovalStep> list = (List<AccountingDossierApprovalStep>) steps;
            return list.stream().anyMatch(step ->
                    "ACCOUNTANT".equals(step.getApproverType())
                            && step.getApproverUserId() == null);
        }));
    }

    @Test
    void testSubmitDossierWithCustomSteps() {
        Long dossierId = 1L;
        Company company = new Company();
        company.setId(10L);

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(dossierId);
        dossier.setStatus(AccountingDossierStatus.DRAFT);
        dossier.setCompany(company);

        User creator = new User();
        creator.setId("user-creator-id");
        creator.setEmail("test-user");

        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));
        when(documentItemRepository.countByDossierIdAndActiveTrue(dossierId)).thenReturn(1L);
        when(userRepository.findByEmail("test-user")).thenReturn(creator);
        when(repository.save(any(AccountingDossier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountingDossierSubmitRequest req = new AccountingDossierSubmitRequest();
        AccountingDossierSubmitRequest.CustomStep step1 = new AccountingDossierSubmitRequest.CustomStep();
        step1.setStepOrder(1);
        step1.setStepName("Bước duyệt tùy chọn");
        step1.setApproverType("CUSTOM_USER");
        step1.setApproverUserId("user-custom-id");
        req.setCustomSteps(List.of(step1));

        ResAccountingDossierDTO result = service.submit(dossierId, req);

        assertEquals(AccountingDossierStatus.SUBMITTED, result.getStatus());
        verify(approvalStepRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testSubmitDossierWithCategoryStepsConfig() {
        Long dossierId = 1L;
        Company company = new Company();
        company.setId(10L);

        AccountingDossierCategory category = new AccountingDossierCategory();
        category.setId(100L);
        category.setApprovalStepsConfig("[{\"stepOrder\":1,\"stepName\":\"Bước mẫu\",\"approverType\":\"ACCOUNTANT\"}]");

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(dossierId);
        dossier.setStatus(AccountingDossierStatus.DRAFT);
        dossier.setCompany(company);
        dossier.setDossierCategory(category);

        User creator = new User();
        creator.setId("user-creator-id");
        creator.setEmail("test-user");

        User accountant = new User();
        accountant.setId("user-accountant-id");

        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));
        when(documentItemRepository.countByDossierIdAndActiveTrue(dossierId)).thenReturn(1L);
        when(userRepository.findByEmail("test-user")).thenReturn(creator);
        when(userRepository.findUsersByPermissionAndCompany(anyList(), anyList(), anyBoolean())).thenReturn(List.of(accountant));
        when(repository.save(any(AccountingDossier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResAccountingDossierDTO result = service.submit(dossierId, null);

        assertEquals(AccountingDossierStatus.SUBMITTED, result.getStatus());
        verify(approvalStepRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testApproveDossierTransition() {
        Long dossierId = 1L;
        Company company = new Company();
        company.setId(10L);

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(dossierId);
        dossier.setStatus(AccountingDossierStatus.IN_REVIEW);
        dossier.setCompany(company);

        User approver = new User();
        approver.setId("user-manager-id");
        approver.setEmail("test-user");

        AccountingDossierApprovalStep step1 = new AccountingDossierApprovalStep();
        step1.setId(101L);
        step1.setStatus("CURRENT");
        step1.setApproverUserId("user-manager-id");
        step1.setStepOrder(1);
        step1.setApproverType("DEPARTMENT_MANAGER");

        AccountingDossierApprovalStep step2 = new AccountingDossierApprovalStep();
        step2.setId(102L);
        step2.setStatus("PENDING");
        step2.setStepOrder(2);
        step2.setApproverType("ACCOUNTANT");
        step2.setApproverUserId("user-accountant-id"); // Set approverUserId so it doesn't try dynamic resolution

        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));
        when(userRepository.findByEmail("test-user")).thenReturn(approver);
        when(approvalStepRepository.findByDossierIdAndActiveTrueOrderByStepOrderAsc(dossierId)).thenReturn(List.of(step1, step2));
        when(repository.save(any(AccountingDossier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.approve(dossierId, null);

        assertEquals("APPROVED", step1.getStatus());
        assertEquals("CURRENT", step2.getStatus());
        verify(approvalStepRepository, atLeastOnce()).save(any(AccountingDossierApprovalStep.class));
    }

    @Test
    void testAccountantRoleCanApproveRoleBasedAccountantStep() {
        Long dossierId = 1L;
        Company company = new Company();
        company.setId(10L);

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(dossierId);
        dossier.setStatus(AccountingDossierStatus.IN_REVIEW);
        dossier.setCompany(company);

        User accountant = new User();
        accountant.setId("accountant-1");
        accountant.setEmail("test-user");
        vn.system.app.modules.role.domain.Role role = new vn.system.app.modules.role.domain.Role();
        role.setName("KETOAN");
        accountant.setRole(role);

        AccountingDossierApprovalStep step = new AccountingDossierApprovalStep();
        step.setId(101L);
        step.setStatus("CURRENT");
        step.setApproverType("ACCOUNTANT");
        step.setStepOrder(2);

        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));
        when(userRepository.findByEmail("test-user")).thenReturn(accountant);
        when(approvalStepRepository.findByDossierIdAndActiveTrueOrderByStepOrderAsc(dossierId)).thenReturn(List.of(step));
        when(repository.save(any(AccountingDossier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResAccountingDossierDTO result = service.approve(dossierId, null);

        assertEquals("APPROVED", step.getStatus());
        assertEquals(AccountingDossierStatus.APPROVED, result.getStatus());
    }

    @Test
    void testApproveFinalDossierLocksApproved() {
        Long dossierId = 1L;
        Company company = new Company();
        company.setId(10L);

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(dossierId);
        dossier.setStatus(AccountingDossierStatus.IN_REVIEW);
        dossier.setCompany(company);

        User approver = new User();
        approver.setId("user-chief-id");
        approver.setEmail("test-user");

        AccountingDossierApprovalStep step1 = new AccountingDossierApprovalStep();
        step1.setId(101L);
        step1.setStatus("CURRENT");
        step1.setApproverUserId("user-chief-id");
        step1.setStepOrder(3);
        step1.setApproverType("CHIEF_ACCOUNTANT");

        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));
        when(userRepository.findByEmail("test-user")).thenReturn(approver);
        when(approvalStepRepository.findByDossierIdAndActiveTrueOrderByStepOrderAsc(dossierId)).thenReturn(List.of(step1));
        when(repository.save(any(AccountingDossier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResAccountingDossierDTO result = service.approve(dossierId, null);

        assertEquals("APPROVED", step1.getStatus());
        assertEquals(AccountingDossierStatus.APPROVED, result.getStatus());
        assertNotNull(result.getApprovedAt());
    }

    @Test
    void testRejectDossierTransitionsToRejected() {
        Long dossierId = 1L;
        Company company = new Company();
        company.setId(10L);

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(dossierId);
        dossier.setStatus(AccountingDossierStatus.IN_REVIEW);
        dossier.setCompany(company);

        User approver = new User();
        approver.setId("user-manager-id");
        approver.setEmail("test-user");

        AccountingDossierApprovalStep step1 = new AccountingDossierApprovalStep();
        step1.setId(101L);
        step1.setStatus("CURRENT");
        step1.setApproverUserId("user-manager-id");
        step1.setApproverType("DEPARTMENT_MANAGER");

        vn.system.app.modules.accountingdossier.domain.request.AccountingDossierActionRequest req = 
            new vn.system.app.modules.accountingdossier.domain.request.AccountingDossierActionRequest();
        req.setNote("Sai chứng từ");

        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));
        when(userRepository.findByEmail("test-user")).thenReturn(approver);
        when(approvalStepRepository.findByDossierIdAndActiveTrueOrderByStepOrderAsc(dossierId)).thenReturn(List.of(step1));
        when(repository.save(any(AccountingDossier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResAccountingDossierDTO result = service.reject(dossierId, req);

        assertEquals("REJECTED", step1.getStatus());
        assertEquals(AccountingDossierStatus.REJECTED, result.getStatus());
    }

    @Test
    void testTerminateDossierTransitionsToTerminated() {
        Long dossierId = 1L;
        Company company = new Company();
        company.setId(10L);

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(dossierId);
        dossier.setStatus(AccountingDossierStatus.IN_REVIEW);
        dossier.setCompany(company);

        User chiefAccountant = new User();
        chiefAccountant.setId("user-chief-id");
        chiefAccountant.setEmail("test-user");
        vn.system.app.modules.role.domain.Role role = new vn.system.app.modules.role.domain.Role();
        role.setName("CHIEF_ACCOUNTANT");
        chiefAccountant.setRole(role);

        vn.system.app.modules.accountingdossier.domain.request.AccountingDossierActionRequest req = 
            new vn.system.app.modules.accountingdossier.domain.request.AccountingDossierActionRequest();
        req.setNote("Gian lận");

        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));
        when(userRepository.findByEmail("test-user")).thenReturn(chiefAccountant);
        when(repository.save(any(AccountingDossier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResAccountingDossierDTO result = service.terminate(dossierId, req);

        assertEquals(AccountingDossierStatus.TERMINATED, result.getStatus());
        assertNotNull(result.getTerminatedAt());
    }

    @Test
    void testSubmitDossierReturnLimitExceededThrowsException() {
        Long dossierId = 1L;
        Company company = new Company();
        company.setId(10L);

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(dossierId);
        dossier.setStatus(AccountingDossierStatus.DRAFT);
        dossier.setReturnCount(3);
        dossier.setCompany(company);
        dossier.setDossierCode("BCT-2026-000001");

        User creator = new User();
        creator.setId("user-creator-id");
        creator.setEmail("test-user");
        creator.setDirectManager(null); // No manager configured

        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));
        when(documentItemRepository.countByDossierIdAndActiveTrue(dossierId)).thenReturn(1L);
        when(userRepository.findByEmail("test-user")).thenReturn(creator);
        when(approvalStepRepository.findByDossierIdAndActiveTrue(dossierId)).thenReturn(Collections.emptyList());

        IdInvalidException exception = assertThrows(IdInvalidException.class, () -> {
            service.submit(dossierId);
        });

        assertTrue(exception.getMessage().contains("Hồ sơ đã bị hoàn trả 3 lần"));
    }

    @Test
    void testFetchPendingMyApprovalUsesApprovalStepSpecification() {
        User currentUser = new User();
        currentUser.setId("user-manager-id");
        currentUser.setEmail("test-user");
        vn.system.app.modules.role.domain.Role role = new vn.system.app.modules.role.domain.Role();
        role.setName("ACCOUNTANT");
        currentUser.setRole(role);

        Company company = new Company();
        company.setId(10L);

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(99L);
        dossier.setCompany(company);
        dossier.setDepartment(new Department());
        dossier.getDepartment().setId(20L);
        dossier.setStatus(AccountingDossierStatus.IN_REVIEW);
        dossier.setActive(true);

        when(userRepository.findByEmail("test-user")).thenReturn(currentUser);
        when(repository.findAll((Specification<AccountingDossier>) any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dossier), PageRequest.of(0, 10), 1));

        var result = service.fetchPendingMyApproval(PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getMeta().getTotal());
        assertEquals(1, ((List<?>) result.getResult()).size());
        verify(repository, times(1)).findAll((Specification<AccountingDossier>) any(), any(Pageable.class));
    }

    @Test
    void testBulkApproveReturnsMixedResults() {
        Long approvedId = 1L;
        Long rejectedId = 2L;

        Company company = new Company();
        company.setId(10L);

        AccountingDossier approvedDossier = new AccountingDossier();
        approvedDossier.setId(approvedId);
        approvedDossier.setStatus(AccountingDossierStatus.IN_REVIEW);
        approvedDossier.setCompany(company);

        AccountingDossier rejectedDossier = new AccountingDossier();
        rejectedDossier.setId(rejectedId);
        rejectedDossier.setStatus(AccountingDossierStatus.IN_REVIEW);
        rejectedDossier.setCompany(company);

        User approver = new User();
        approver.setId("user-approver-id");
        approver.setEmail("test-user");

        AccountingDossierApprovalStep approvedStep = new AccountingDossierApprovalStep();
        approvedStep.setId(11L);
        approvedStep.setStatus("CURRENT");
        approvedStep.setApproverUserId("user-approver-id");
        approvedStep.setApproverType("ACCOUNTANT");
        approvedStep.setStepOrder(1);

        AccountingDossierApprovalStep rejectedStep = new AccountingDossierApprovalStep();
        rejectedStep.setId(12L);
        rejectedStep.setStatus("CURRENT");
        rejectedStep.setApproverUserId("different-user-id");
        rejectedStep.setApproverType("ACCOUNTANT");
        rejectedStep.setStepOrder(1);

        when(repository.findById(approvedId)).thenReturn(Optional.of(approvedDossier));
        when(repository.findById(rejectedId)).thenReturn(Optional.of(rejectedDossier));
        when(userRepository.findByEmail("test-user")).thenReturn(approver);
        when(approvalStepRepository.findByDossierIdAndActiveTrueOrderByStepOrderAsc(approvedId))
                .thenReturn(List.of(approvedStep));
        when(approvalStepRepository.findByDossierIdAndActiveTrueOrderByStepOrderAsc(rejectedId))
                .thenReturn(List.of(rejectedStep));
        when(repository.save(any(AccountingDossier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResAccountingDossierBulkActionDTO result = service.bulkApprove(List.of(approvedId, rejectedId), null);

        assertNotNull(result.getBulkActionId());
        assertEquals(2, result.getTotal());
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertEquals(2, result.getItems().size());
        assertTrue(result.getItems().get(0).isSuccess());
        assertFalse(result.getItems().get(1).isSuccess());
        assertTrue(result.getItems().get(1).getError().contains("không có quyền"));
    }

    @Test
    void testBulkCheckDocumentsUpdatesMatchedDocuments() {
        Long dossierId = 1L;

        Company company = new Company();
        company.setId(10L);

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(dossierId);
        dossier.setCompany(company);
        dossier.setStatus(AccountingDossierStatus.IN_REVIEW);
        dossier.setActive(true);

        User accountant = new User();
        accountant.setId("user-accountant-id");
        accountant.setEmail("test-user");

        AccountingDossierApprovalStep step = new AccountingDossierApprovalStep();
        step.setStatus("CURRENT");
        step.setApproverType("ACCOUNTANT");
        step.setApproverUserId("user-accountant-id");

        AccountingDossierDocument doc = new AccountingDossierDocument();
        doc.setId(55L);
        doc.setDocumentName("Hóa đơn");
        doc.setCheckStatus("VALID");

        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));
        when(userRepository.findByEmail("test-user")).thenReturn(accountant);
        when(approvalStepRepository.findByDossierIdAndActiveTrueOrderByStepOrderAsc(dossierId))
                .thenReturn(List.of(step));
        when(documentItemRepository.findByIdInAndDossierIdAndActiveTrue(List.of(55L), dossierId))
                .thenReturn(List.of(doc));
        when(documentItemRepository.save(any(AccountingDossierDocument.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResAccountingDossierBulkActionDTO result = service.bulkCheckDocuments(
                dossierId,
                List.of(55L),
                "NEED_SUPPLEMENT",
                "Thiếu file đính kèm");

        assertEquals("NEED_SUPPLEMENT", doc.getCheckStatus());
        assertEquals("Thiếu file đính kèm", doc.getCheckNote());
        assertNotNull(result.getBulkActionId());
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        verify(documentItemRepository, times(1)).save(doc);
    }

    @Test
    void testBulkCheckTemplateDossierCannotMarkNotRequired() {
        Long dossierId = 1L;

        Company company = new Company();
        company.setId(10L);

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(dossierId);
        dossier.setCompany(company);
        dossier.setStatus(AccountingDossierStatus.IN_REVIEW);
        dossier.setCategoryMode(AccountingDossierCategoryMode.TEMPLATE);
        dossier.setActive(true);

        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));

        PermissionException exception = assertThrows(PermissionException.class, () ->
                service.bulkCheckDocuments(dossierId, List.of(55L), "NOT_REQUIRED", null));

        assertTrue(exception.getMessage().contains("Không yêu cầu"));
        verify(documentItemRepository, never()).save(any());
    }

    @Test
    void testReviewDocumentRequiresCurrentAccountantStep() {
        Long dossierId = 1L;
        Long docId = 55L;

        Company company = new Company();
        company.setId(10L);

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(dossierId);
        dossier.setCompany(company);
        dossier.setStatus(AccountingDossierStatus.IN_REVIEW);
        dossier.setActive(true);

        User manager = new User();
        manager.setId("manager-id");
        manager.setEmail("test-user");

        AccountingDossierApprovalStep step = new AccountingDossierApprovalStep();
        step.setStatus("CURRENT");
        step.setApproverType("DEPARTMENT_MANAGER");
        step.setApproverUserId("manager-id");

        vn.system.app.modules.accountingdossier.domain.request.AccountingDossierDocumentCheckRequest req =
                new vn.system.app.modules.accountingdossier.domain.request.AccountingDossierDocumentCheckRequest();
        req.setCheckStatus("VALID");

        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));
        when(userRepository.findByEmail("test-user")).thenReturn(manager);
        when(approvalStepRepository.findByDossierIdAndActiveTrueOrderByStepOrderAsc(dossierId))
                .thenReturn(List.of(step));

        PermissionException exception = assertThrows(PermissionException.class, () ->
                service.reviewDocument(dossierId, docId, req));

        assertTrue(exception.getMessage().contains("kế toán kiểm tra"));
    }

    @Test
    void testHandleReturnResponseRequiresCurrentApprover() {
        Long dossierId = 1L;
        Company company = new Company();
        company.setId(10L);

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(dossierId);
        dossier.setCompany(company);
        dossier.setStatus(AccountingDossierStatus.RETURN_REQUESTED);
        dossier.setCreatorId("creator-id");
        dossier.setReturnCount(0);

        User approver = new User();
        approver.setId("accountant-id");
        approver.setEmail("test-user");

        AccountingDossierApprovalStep step = new AccountingDossierApprovalStep();
        step.setStatus("CURRENT");
        step.setApproverType("ACCOUNTANT");
        step.setApproverUserId("accountant-id");

        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));
        when(userRepository.findByEmail("test-user")).thenReturn(approver);
        when(approvalStepRepository.findByDossierIdAndActiveTrueOrderByStepOrderAsc(dossierId))
                .thenReturn(List.of(step));
        when(repository.save(any(AccountingDossier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResAccountingDossierDTO result = service.handleReturnResponse(dossierId, "ACCEPT", null);

        assertEquals(AccountingDossierStatus.RETURNED, result.getStatus());
        assertEquals("RETURNED", step.getStatus());
        assertEquals(1, dossier.getReturnCount());
    }

    @Test
    void testRejectTemplateSyncTurnsOffSyncRequest() {
        Long dossierId = 1L;
        Company company = new Company();
        company.setId(10L);

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(dossierId);
        dossier.setCompany(company);
        dossier.setCategoryMode(AccountingDossierCategoryMode.UNSTRUCTURED);
        dossier.setSyncCategoryRequested(true);

        User chief = new User();
        chief.setId("user-chief-id");
        chief.setEmail("test-user");
        vn.system.app.modules.role.domain.Role role = new vn.system.app.modules.role.domain.Role();
        role.setName("CHIEF_ACCOUNTANT");
        chief.setRole(role);

        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));
        when(userRepository.findByEmail("test-user")).thenReturn(chief);
        when(repository.save(any(AccountingDossier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResAccountingDossierDTO result = service.rejectTemplateSync(dossierId, null);

        assertFalse(dossier.isSyncCategoryRequested());
        assertFalse(result.isSyncCategoryRequested());
        verify(repository, times(1)).save(dossier);
    }

    @Test
    void archive_Success() {
        Long id = 1L;
        vn.system.app.modules.accountingdossier.domain.request.AccountingDossierActionRequest req = new vn.system.app.modules.accountingdossier.domain.request.AccountingDossierActionRequest();
        req.setNote("Archive this");

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(id);
        dossier.setStatus(AccountingDossierStatus.APPROVED);
        dossier.setActive(true);
        vn.system.app.modules.company.domain.Company company = new vn.system.app.modules.company.domain.Company();
        company.setId(100L);
        dossier.setCompany(company);

        when(repository.findById(id)).thenReturn(Optional.of(dossier));
        when(repository.save(any(AccountingDossier.class))).thenReturn(dossier);

        ResAccountingDossierDTO res = service.archive(id, req);

        assertNotNull(res);
        assertEquals(vn.system.app.modules.accountingdossier.domain.enums.AccountingDossierStorageStatus.ARCHIVED, dossier.getStorageStatus());
        verify(repository, times(1)).save(dossier);
        verify(auditLogRepository, times(1)).save(any());
    }

    @Test
    void refreshExpiredRetentionStatuses_Success() {
        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(1L);
        dossier.setStorageStatus(vn.system.app.modules.accountingdossier.domain.enums.AccountingDossierStorageStatus.IN_RETENTION);

        when(repository.findByActiveTrueAndStorageStatusAndRetentionUntilBefore(
                eq(vn.system.app.modules.accountingdossier.domain.enums.AccountingDossierStorageStatus.IN_RETENTION),
                any(java.time.Instant.class)
        )).thenReturn(List.of(dossier));

        int count = service.refreshExpiredRetentionStatuses();

        assertEquals(1, count);
        assertEquals(vn.system.app.modules.accountingdossier.domain.enums.AccountingDossierStorageStatus.EXPIRED, dossier.getStorageStatus());
        verify(repository, times(1)).saveAll(any());
    }

    @Test
    void getStorageSummary_Success() {
        when(repository.countByActiveTrue()).thenReturn(10L);
        when(repository.countByActiveTrueAndStorageStatus(vn.system.app.modules.accountingdossier.domain.enums.AccountingDossierStorageStatus.IN_RETENTION)).thenReturn(5L);
        when(repository.countByActiveTrueAndStorageStatus(vn.system.app.modules.accountingdossier.domain.enums.AccountingDossierStorageStatus.EXPIRED)).thenReturn(3L);
        when(repository.countByActiveTrueAndStorageStatus(vn.system.app.modules.accountingdossier.domain.enums.AccountingDossierStorageStatus.ARCHIVED)).thenReturn(2L);
        
        vn.system.app.modules.accountingdossier.domain.response.ResAccountingDossierStorageSummaryDTO res = service.getStorageSummary();
        
        assertNotNull(res);
        assertEquals(10L, res.getTotal());
        assertEquals(5L, res.getInRetention());
        assertEquals(3L, res.getExpired());
        assertEquals(2L, res.getArchived());
    }

    @Test
    void reportByStatus_Success() {
        Object[] row1 = new Object[]{AccountingDossierStatus.SUBMITTED, 5L};
        when(repository.countActiveGroupByStatus()).thenReturn(Collections.singletonList(row1));

        List<vn.system.app.modules.accountingdossier.domain.response.ResAccountingDossierReportRowDTO> res = service.reportByStatus();
        
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals("SUBMITTED", res.get(0).getKey());
        assertEquals(5L, res.get(0).getCount());
    }

    @Test
    void testSubmitDossierWithReturnCountGe3ForcedManager() {
        Long dossierId = 1L;
        Company company = new Company();
        company.setId(10L);
        company.setCode("CMP");
        company.setName("Test Company");

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(dossierId);
        dossier.setStatus(AccountingDossierStatus.DRAFT);
        dossier.setCompany(company);
        dossier.setReturnCount(3);

        User creator = new User();
        creator.setId("creator-id");
        creator.setEmail("test-user");

        User manager = new User();
        manager.setId("manager-id");
        creator.setDirectManager(manager);

        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));
        when(documentItemRepository.countByDossierIdAndActiveTrue(dossierId)).thenReturn(1L);
        when(userRepository.findByEmail("test-user")).thenReturn(creator);
        when(repository.save(any(AccountingDossier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResAccountingDossierDTO result = service.submit(dossierId, null);

        assertNotNull(result);
        assertEquals(AccountingDossierStatus.SUBMITTED, dossier.getStatus());
        verify(approvalStepRepository, times(1)).saveAll(argThat(steps -> {
            List<AccountingDossierApprovalStep> list = (List<AccountingDossierApprovalStep>) steps;
            return list.stream().anyMatch(s -> "DEPARTMENT_MANAGER".equals(s.getApproverType()) && "manager-id".equals(s.getApproverUserId()) && !"SKIPPED".equals(s.getStatus()));
        }));
    }

    @Test
    void testSubmitDossierWithReturnCountGe3ForcedManagerMissingManager() {
        Long dossierId = 1L;
        Company company = new Company();
        company.setId(10L);
        company.setCode("CMP");
        company.setName("Test Company");

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(dossierId);
        dossier.setStatus(AccountingDossierStatus.DRAFT);
        dossier.setCompany(company);
        dossier.setReturnCount(3);

        User creator = new User();
        creator.setId("creator-id");
        creator.setEmail("test-user");
        creator.setDirectManager(null);

        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));
        when(documentItemRepository.countByDossierIdAndActiveTrue(dossierId)).thenReturn(1L);
        when(userRepository.findByEmail("test-user")).thenReturn(creator);

        assertThrows(IdInvalidException.class, () -> service.submit(dossierId, null));
    }

    @Test
    void testQuickAddDocumentSubmittedDossierReturnsToCreator() {
        Long dossierId = 1L;
        Company company = new Company();
        company.setId(10L);

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(dossierId);
        dossier.setStatus(AccountingDossierStatus.SUBMITTED);
        dossier.setCompany(company);
        dossier.setReturnCount(1);
        dossier.setActive(true);
        dossier.setCreatorId("creator-id");

        User creator = new User();
        creator.setId("creator-id");
        creator.setEmail("test-user");

        AccountingDossierDocumentRequest req = new AccountingDossierDocumentRequest();
        req.setAccountingCategoryId(100L);
        req.setDocumentName("Hóa đơn VAT");
        req.setDocumentType("PDF");

        AccountingDocumentCategory category = new AccountingDocumentCategory();
        category.setId(100L);

        AccountingDossierDocument item = new AccountingDossierDocument();
        item.setId(5L);
        item.setDossier(dossier);
        item.setDocumentName("Hóa đơn VAT");

        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));
        when(userRepository.findByEmail("test-user")).thenReturn(creator);
        when(accountingCategoryRepository.findById(100L)).thenReturn(Optional.of(category));
        when(documentItemRepository.save(any())).thenReturn(item);

        vn.system.app.modules.accountingdossier.domain.response.ResAccountingDossierDocumentDTO res = service.addDocument(dossierId, req);

        assertNotNull(res);
        assertEquals(AccountingDossierStatus.RETURNED, dossier.getStatus());
        assertEquals(2, dossier.getReturnCount());
        verify(repository, times(1)).save(dossier);
        verify(auditLogRepository, times(1)).save(any());
    }

    @Test
    void testTerminateDossierByNonChiefThrowsException() {
        Long dossierId = 1L;
        Company company = new Company();
        company.setId(10L);

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(dossierId);
        dossier.setStatus(AccountingDossierStatus.IN_REVIEW);
        dossier.setCompany(company);

        User accountant = new User();
        accountant.setId("user-accountant-id");
        accountant.setEmail("test-user");
        vn.system.app.modules.role.domain.Role role = new vn.system.app.modules.role.domain.Role();
        role.setName("KETOAN");
        accountant.setRole(role);

        vn.system.app.modules.accountingdossier.domain.request.AccountingDossierActionRequest req =
                new vn.system.app.modules.accountingdossier.domain.request.AccountingDossierActionRequest();
        req.setNote("Gian lận");

        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));
        when(userRepository.findByEmail(anyString())).thenReturn(accountant);

        PermissionException exception = assertThrows(PermissionException.class, () ->
                service.terminate(dossierId, req));

        assertTrue(exception.getMessage().contains("Super Admin mới được quyền chấm dứt"));
    }

    @Test
    void testApproveDossierByNonCurrentApproverThrowsException() {
        Long dossierId = 1L;
        Company company = new Company();
        company.setId(10L);

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(dossierId);
        dossier.setStatus(AccountingDossierStatus.IN_REVIEW);
        dossier.setCompany(company);

        User randomUser = new User();
        randomUser.setId("user-random-id");
        randomUser.setEmail("test-user");
        vn.system.app.modules.role.domain.Role role = new vn.system.app.modules.role.domain.Role();
        role.setName("EMPLOYEE");
        randomUser.setRole(role);

        AccountingDossierApprovalStep step1 = new AccountingDossierApprovalStep();
        step1.setId(101L);
        step1.setStatus("CURRENT");
        step1.setApproverUserId("user-manager-id");
        step1.setApproverType("DEPARTMENT_MANAGER");
        step1.setStepOrder(1);

        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));
        when(userRepository.findByEmail("test-user")).thenReturn(randomUser);
        when(approvalStepRepository.findByDossierIdAndActiveTrueOrderByStepOrderAsc(dossierId)).thenReturn(List.of(step1));

        PermissionException exception = assertThrows(PermissionException.class, () ->
                service.approve(dossierId, null));

        assertTrue(exception.getMessage().contains("Bạn không có quyền xử lý"));
    }

    @Test
    void testArchiveDossierNotApprovedThrowsException() {
        Long dossierId = 1L;
        Company company = new Company();
        company.setId(10L);

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(dossierId);
        dossier.setCompany(company);
        dossier.setStatus(AccountingDossierStatus.IN_REVIEW);

        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));

        PermissionException exception = assertThrows(PermissionException.class, () ->
                service.archive(dossierId, null));

        assertTrue(exception.getMessage().contains("Chỉ bộ chứng từ đã duyệt"));
    }

    @Test
    void testAddDocumentDuplicateInvoiceWarning() {
        Long dossierId = 1L;
        Company company = new Company();
        company.setId(10L);

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(dossierId);
        dossier.setStatus(AccountingDossierStatus.DRAFT);
        dossier.setCompany(company);
        dossier.setActive(true);
        dossier.setCreatorId("creator-id");

        User creator = new User();
        creator.setId("creator-id");
        creator.setEmail("test-user");

        AccountingDossierDocumentRequest req = new AccountingDossierDocumentRequest();
        req.setAccountingCategoryId(100L);
        req.setDocumentName("Hóa đơn");
        req.setInvoiceNumber("INV-001");
        req.setPartnerName("Partner A");

        AccountingDocumentCategory category = new AccountingDocumentCategory();
        category.setId(100L);

        AccountingDossier duplicateDossier = new AccountingDossier();
        duplicateDossier.setId(2L);
        duplicateDossier.setDossierCode("BCT-2");

        AccountingDossierDocument duplicateDoc = new AccountingDossierDocument();
        duplicateDoc.setId(99L);
        duplicateDoc.setInvoiceNumber("INV-001");
        duplicateDoc.setPartnerName("Partner A");
        duplicateDoc.setDossier(duplicateDossier);

        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));
        when(userRepository.findByEmail("test-user")).thenReturn(creator);
        when(accountingCategoryRepository.findById(100L)).thenReturn(Optional.of(category));
        when(documentItemRepository.findDuplicateInvoices(eq("INV-001"), eq("Partner A"), eq(dossierId), any()))
                .thenReturn(List.of(duplicateDoc));

        assertThrows(DuplicateInvoiceWarningException.class, () -> service.addDocument(dossierId, req));
    }

    @Test
    void testAddDocumentDuplicateInvoiceConfirmed() {
        Long dossierId = 1L;
        Company company = new Company();
        company.setId(10L);

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(dossierId);
        dossier.setStatus(AccountingDossierStatus.DRAFT);
        dossier.setCompany(company);
        dossier.setActive(true);
        dossier.setCreatorId("creator-id");

        User creator = new User();
        creator.setId("creator-id");
        creator.setEmail("test-user");

        AccountingDossierDocumentRequest req = new AccountingDossierDocumentRequest();
        req.setAccountingCategoryId(100L);
        req.setDocumentName("Hóa đơn");
        req.setInvoiceNumber("INV-001");
        req.setPartnerName("Partner A");
        req.setConfirmDuplicate(true);
        req.setDuplicateReason("Lý do hợp lệ");

        AccountingDocumentCategory category = new AccountingDocumentCategory();
        category.setId(100L);

        AccountingDossierDocument savedDoc = new AccountingDossierDocument();
        savedDoc.setId(5L);
        savedDoc.setDossier(dossier);
        savedDoc.setDocumentName("Hóa đơn");

        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));
        when(userRepository.findByEmail("test-user")).thenReturn(creator);
        when(accountingCategoryRepository.findById(100L)).thenReturn(Optional.of(category));
        when(documentItemRepository.save(any())).thenReturn(savedDoc);

        var res = service.addDocument(dossierId, req);

        assertNotNull(res);
        verify(documentItemRepository, times(1)).save(any());
        verify(auditLogRepository, times(2)).save(any());
    }

    @Test
    void testGetByQrTokenSuccess() {
        String token = "qr-token-123";
        Company company = new Company();
        company.setId(10L);

        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(1L);
        dossier.setQrToken(token);
        dossier.setActive(true);
        dossier.setCompany(company);

        when(repository.findByQrToken(token)).thenReturn(Optional.of(dossier));

        ResAccountingDossierDTO res = service.getByQrToken(token);

        assertNotNull(res);
        assertEquals(token, res.getQrToken());
    }

    @Test
    void testGetByQrTokenNotFound() {
        String token = "invalid-token";
        when(repository.findByQrToken(token)).thenReturn(Optional.empty());

        assertThrows(IdInvalidException.class, () -> service.getByQrToken(token));
    }

    @Test
    void testGetByQrTokenInactive() {
        String token = "qr-token-inactive";
        AccountingDossier dossier = new AccountingDossier();
        dossier.setId(1L);
        dossier.setQrToken(token);
        dossier.setActive(false);

        when(repository.findByQrToken(token)).thenReturn(Optional.of(dossier));

        assertThrows(IdInvalidException.class, () -> service.getByQrToken(token));
    }
}
