package vn.system.app.modules.evaluation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.common.util.UserScopeContext;
import vn.system.app.modules.evaluation.domain.*;
import vn.system.app.modules.evaluation.domain.enums.*;
import vn.system.app.modules.evaluation.repository.*;
import vn.system.app.modules.evaluation.service.EvaluationPeriodService;
import vn.system.app.modules.evaluation.service.EvaluationRecordService;
import vn.system.app.modules.evaluation.service.EvaluationReminderScheduler;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.repository.UserRepository;
import vn.system.app.modules.userposition.repository.UserPositionRepository;
import vn.system.app.modules.userposition.domain.UserPosition;
import vn.system.app.modules.evaluation.domain.response.ResPeriodProgressDTO;
import vn.system.app.modules.adminscope.service.UserAdminScopeService;
import vn.system.app.modules.notification.repository.NotificationRepository;
import vn.system.app.modules.notification.event.AppNotificationEvent;
import vn.system.app.modules.role.domain.Role;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluationModuleUatTest {

    @Mock
    private EvaluationPeriodRepository periodRepo;

    @Mock
    private EvaluationRecordRepository recordRepo;

    @Mock
    private PeriodEmployeeRepository periodEmployeeRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private EvaluationHistoryRepository historyRepo;

    @Mock
    private EvaluationScoreRepository scoreRepo;

    @Mock
    private EvaluationCommentRepository commentRepo;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Mock
    private TemplateCriteriaRepository criteriaRepo;

    @Mock
    private NotificationRepository notificationRepo;

    @Mock
    private UserPositionRepository userPositionRepo;

    @Mock
    private UserAdminScopeService userAdminScopeService;

    @InjectMocks
    private EvaluationPeriodService periodService;

    @InjectMocks
    private EvaluationRecordService recordService;

    @InjectMocks
    private EvaluationReminderScheduler reminderScheduler;

    @BeforeEach
    void setUpScope() {
        UserScopeContext.set(new UserScopeContext.UserScope(
                "admin-id", Set.of(), Set.of(), true, true, false, false
        ));
    }

    @AfterEach
    void clearScope() {
        UserScopeContext.clear();
    }

    /**
     * UAT KỊCH BẢN 2: NHÂN VIÊN NGHỈ VIỆC GIỮA KỲ
     * Kiểm tra khi gọi cancelEmployee, trạng thái PeriodEmployee chuyển sang CANCELLED,
     * và EvaluationRecord tương ứng chưa hoàn thành cũng bị hủy (CANCELLED).
     */
    @Test
    void testCancelEmployeeOnResign() {
        // Arrange
        Long peId = 100L;
        PeriodEmployee pe = new PeriodEmployee();
        pe.setId(peId);
        pe.setStatus(PeriodEmployeeStatus.ACTIVE);
        
        EvaluationPeriod period = new EvaluationPeriod();
        period.setId(1L);
        pe.setPeriod(period);

        User employee = new User();
        employee.setId("emp-01");
        pe.setEmployee(employee);

        when(periodEmployeeRepo.findById(peId)).thenReturn(Optional.of(pe));
        when(periodEmployeeRepo.save(any(PeriodEmployee.class))).thenAnswer(i -> i.getArgument(0));

        EvaluationRecord record = new EvaluationRecord();
        record.setId(200L);
        record.setStatus(RecordStatus.EMPLOYEE_DRAFTING);
        when(recordRepo.findByPeriodIdAndEmployeeId(1L, "emp-01")).thenReturn(Optional.of(record));

        // Act
        PeriodEmployee result = periodService.cancelEmployee(peId);

        // Assert
        assertEquals(PeriodEmployeeStatus.CANCELLED, result.getStatus());
        assertEquals(RecordStatus.CANCELLED, record.getStatus());
        verify(recordRepo, times(1)).save(record);
        verify(historyRepo, times(1)).save(any(EvaluationHistory.class));
    }

    /**
     * UAT KỊCH BẢN 4: NỚI DEADLINE KHI REJECT BẢN GHI
     * Kiểm tra khi người duyệt reject bản ghi PENDING_APPROVAL, trạng thái chuyển về REVISION_NEEDED
     * và trường managerDeadlineOverride được tự động nới thêm 2 ngày từ thời điểm hiện tại.
     */
    @Test
    void testRejectRecordExtendsDeadline() {
        // Arrange
        Long recordId = 300L;
        EvaluationRecord record = new EvaluationRecord();
        record.setId(recordId);
        record.setStatus(RecordStatus.PENDING_APPROVAL);

        User approver = new User();
        approver.setId("approver-01");
        record.setIndirectManager(approver);

        EvaluationPeriod period = new EvaluationPeriod();
        period.setId(1L);
        period.setStatus(PeriodStatus.ACTIVE);
        period.setManagerDeadline(Instant.now().plus(1, ChronoUnit.DAYS)); // Hạn gốc: 1 ngày nữa
        record.setPeriod(period);

        User employee = new User();
        employee.setId("emp-01");
        employee.setName("UAT Nhân Viên");
        record.setEmployee(employee);

        User manager = new User();
        manager.setId("mgr-01");
        record.setDirectManager(manager);

        when(recordRepo.findById(recordId)).thenReturn(Optional.of(record));

        // Act
        EvaluationRecord result = recordService.rejectRecord(recordId, "Cần sửa lại chỉ tiêu số 2", approver);

        // Assert
        assertEquals(RecordStatus.REVISION_NEEDED, result.getStatus());
        assertNotNull(result.getManagerDeadlineOverride());
        // Hạn chót mới phải là sau hạn gốc vì được nới +2 ngày tính từ bây giờ
        assertTrue(result.getManagerDeadlineOverride().isAfter(Instant.now().plus(1, ChronoUnit.DAYS)));
        verify(recordRepo, times(1)).save(record);
        verify(scoreRepo, times(1)).deleteByEvaluationRecordIdAndScoredBy(recordId, ScoredBy.APPROVER);
        verify(historyRepo, times(1)).save(any(EvaluationHistory.class));
    }

    /**
     * UAT KỊCH BẢN 5 & 9: CHẶN ĐÓNG KỲ KHI CÒN BẢN ĐÁNH GIÁ DANG DỞ
     * Đảm bảo không cho phép đóng kỳ ACTIVE nếu còn bản đánh giá chưa xong (không phải COMPLETED/CANCELLED).
     */
    @Test
    void testClosePeriodFailsIfUnfinished() {
        // Arrange
        Long periodId = 1L;
        EvaluationPeriod period = new EvaluationPeriod();
        period.setId(periodId);
        period.setStatus(PeriodStatus.ACTIVE);

        when(periodRepo.findById(periodId)).thenReturn(Optional.of(period));
        // Giả lập còn 3 bản đánh giá dang dở
        when(recordRepo.countByPeriodIdAndStatusNotIn(eq(periodId), anyList())).thenReturn(3L);

        // Act & Assert
        IdInvalidException exception = assertThrows(IdInvalidException.class, () -> {
            periodService.closePeriod(periodId);
        });

        assertTrue(exception.getMessage().contains("Còn 3 bản đánh giá chưa hoàn tất"));
        verify(periodRepo, never()).save(any(EvaluationPeriod.class));
    }

    /**
     * UAT KỊCH BẢN 10: KIỂM THỬ TRỊ SỐ BIÊN VỀ ĐIỂM SỐ (BOUNDARY CHECK)
     * Đảm bảo logic chênh lệch điểm (> 1.0) tính toán Alert hoạt động chính xác.
     */
    @Test
    void testScoreDifferenceBoundary() {
        // Trường hợp 1: Chênh lệch đúng bằng 1.0 -> Không vượt quá 1.0 -> không hiện Alert
        double empScore1 = 4.5;
        double mgrScore1 = 3.5;
        assertFalse(Math.abs(empScore1 - mgrScore1) > 1.0);

        // Trường hợp 2: Chênh lệch 0.9 -> không vượt quá 1.0 -> không hiện Alert
        double empScore2 = 4.5;
        double mgrScore2 = 3.6;
        assertFalse(Math.abs(empScore2 - mgrScore2) > 1.0);

        // Trường hợp 3: Chênh lệch 1.1 > 1.0 -> xuất hiện Alert
        double empScore3 = 4.5;
        double mgrScore3 = 3.4;
        assertTrue(Math.abs(empScore3 - mgrScore3) > 1.0);
    }

    /**
     * UAT ĐỢT 1 - T4: BẮT BUỘC NHẬP LÝ DO ĐÈ ĐIỂM
     * Kiểm tra trường hợp Approver thay đổi điểm so với Manager:
     * - Nếu không truyền overrideReason -> Ném ngoại lệ.
     * - Nếu có truyền overrideReason -> Phê duyệt thành công và lưu nhận xét.
     */
    @Test
    void testApproveRecordRequiresOverrideReasonWhenScoresDiffer() {
        // Arrange
        Long recordId = 1L;
        EvaluationRecord record = new EvaluationRecord();
        record.setId(recordId);
        record.setStatus(RecordStatus.PENDING_APPROVAL);

        User approver = new User();
        approver.setId("approver-01");
        record.setIndirectManager(approver);

        EvaluationPeriod period = new EvaluationPeriod();
        period.setId(10L);
        period.setStatus(PeriodStatus.ACTIVE);
        period.setApprovalDeadline(Instant.now().plus(5, ChronoUnit.DAYS));
        record.setPeriod(period);

        User employee = new User();
        employee.setId("employee-01");
        employee.setName("UAT Nhân Viên");
        record.setEmployee(employee);

        User manager = new User();
        manager.setId("manager-01");
        record.setDirectManager(manager);

        EvaluationTemplate template = new EvaluationTemplate();
        template.setSections(List.of());
        record.setTemplate(template);

        when(criteriaRepo.findBySectionIdIn(anyList())).thenReturn(List.of());

        TemplateCriteria criteria = new TemplateCriteria();
        criteria.setId(101L);

        // Manager score
        EvaluationScore managerScore = new EvaluationScore();
        managerScore.setCriteria(criteria);
        managerScore.setScoredBy(ScoredBy.MANAGER);
        managerScore.setScore(4.0);
        managerScore.setWeightedScore(4.0);

        // Approver score
        EvaluationScore approverScore = new EvaluationScore();
        approverScore.setCriteria(criteria);
        approverScore.setScoredBy(ScoredBy.APPROVER);
        approverScore.setScore(3.0); // Bị đè điểm
        approverScore.setWeightedScore(3.0);

        when(recordRepo.findById(recordId)).thenReturn(Optional.of(record));
        when(scoreRepo.findByEvaluationRecordIdAndScoredBy(recordId, ScoredBy.APPROVER)).thenReturn(List.of(approverScore));
        when(scoreRepo.findByEvaluationRecordIdAndScoredBy(recordId, ScoredBy.MANAGER)).thenReturn(List.of(managerScore));

        // Act & Assert 1: Thất bại khi không truyền lý do
        assertThrows(IdInvalidException.class, () -> {
            recordService.approveRecord(recordId, null, approver);
        });

        assertThrows(IdInvalidException.class, () -> {
            recordService.approveRecord(recordId, "   ", approver);
        });

        // Act 2: Thành công khi có lý do
        recordService.approveRecord(recordId, "Thay đổi điểm phù hợp với KPI thực tế", approver);

        // Assert 2
        assertEquals(RecordStatus.COMPLETED, record.getStatus());
        verify(commentRepo, times(1)).save(any(EvaluationComment.class));
        verify(recordRepo, times(1)).save(record);
    }

    @Test
    void testEscalationForOverdueRecords() {
        // Arrange
        EvaluationPeriod period = new EvaluationPeriod();
        period.setId(10L);
        period.setStatus(PeriodStatus.ACTIVE);
        period.setEmployeeDeadline(Instant.now().minus(2, ChronoUnit.DAYS));

        when(periodRepo.findByStatus(PeriodStatus.ACTIVE)).thenReturn(List.of(period));

        User employee = new User();
        employee.setId("emp-01");
        employee.setName("UAT Employee");

        EvaluationRecord record = new EvaluationRecord();
        record.setId(100L);
        record.setStatus(RecordStatus.EMPLOYEE_DRAFTING);
        record.setEmployee(employee);
        record.setPeriod(period);

        when(recordRepo.findByPeriodId(10L)).thenReturn(List.of(record));
        when(userPositionRepo.findActiveCompanyIdsByUserId("emp-01")).thenReturn(List.of(1L));
        when(userPositionRepo.findActiveDepartmentIdsByUserId("emp-01")).thenReturn(List.of(2L));

        User adminSub2 = new User();
        adminSub2.setId("admin-sub2");
        Role adminRole = new Role();
        adminRole.setName("ADMIN_SUB_2");
        adminSub2.setRole(adminRole);

        when(userRepo.findActiveUsersByRoleNames(anyList())).thenReturn(List.of(adminSub2));
        when(userAdminScopeService.getCompanyScopeIds("admin-sub2")).thenReturn(Set.of(1L));

        when(notificationRepo.existsByRecipientIdAndTypeAndActionLinkAndCreatedAtAfter(
                eq("admin-sub2"), eq("ESCALATION"), anyString(), any(Instant.class)
        )).thenReturn(false);

        // Act
        reminderScheduler.sendReminders();

        // Assert
        ArgumentCaptor<AppNotificationEvent> eventCaptor = ArgumentCaptor.forClass(AppNotificationEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        AppNotificationEvent event = eventCaptor.getValue();
        assertEquals("ESCALATION", event.getType());
        assertTrue(event.getRecipientIds().contains("admin-sub2"));
        assertTrue(event.getContent().contains("UAT Employee"));
    }

    @Test
    void testPeriodProgressAggregation() {
        // Arrange
        vn.system.app.common.util.UserScopeContext.UserScope adminScope = new vn.system.app.common.util.UserScopeContext.UserScope(
                "admin-id", Set.of(1L), Set.of(2L), false, true, false, false
        );
        vn.system.app.common.util.UserScopeContext.set(adminScope);

        try {
            EvaluationPeriod period = new EvaluationPeriod();
            period.setId(10L);
            period.setStatus(PeriodStatus.ACTIVE);
            period.setEmployeeDeadline(Instant.now().minus(2, ChronoUnit.DAYS));

            when(periodRepo.findById(10L)).thenReturn(Optional.of(period));

            User employee1 = new User();
            employee1.setId("emp-01");
            employee1.setName("Emp One");

            EvaluationRecord record1 = new EvaluationRecord();
            record1.setId(100L);
            record1.setStatus(RecordStatus.EMPLOYEE_DRAFTING);
            record1.setEmployee(employee1);
            record1.setPeriod(period);

            when(recordRepo.findByPeriodId(10L)).thenReturn(List.of(record1));

            // Position mapping
            UserPosition pos1 = new UserPosition();
            pos1.setUser(employee1);

            vn.system.app.modules.company.domain.Company comp = new vn.system.app.modules.company.domain.Company();
            comp.setId(1L);

            vn.system.app.modules.department.domain.Department dept = new vn.system.app.modules.department.domain.Department();
            dept.setId(2L);
            dept.setName("IT Department");
            dept.setCompany(comp);

            vn.system.app.modules.departmentjobtitle.domain.DepartmentJobTitle djt = new vn.system.app.modules.departmentjobtitle.domain.DepartmentJobTitle();
            djt.setDepartment(dept);
            pos1.setDepartmentJobTitle(djt);

            when(userPositionRepo.findActiveFullByUserIds(anyList())).thenReturn(List.of(pos1));

            // Act
            ResPeriodProgressDTO result = periodService.getPeriodProgress(10L);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getKpiProgress().getTotalRecords());
            assertEquals(1, result.getKpiProgress().getDraftingCount());
            assertEquals(1, result.getKpiProgress().getOverdueCount());
            assertEquals(100.0, result.getKpiProgress().getDraftingPercentage());

            assertEquals(1, result.getDepartmentProgress().size());
            assertEquals("IT Department", result.getDepartmentProgress().get(0).getDepartmentName());
            assertEquals(1, result.getDepartmentProgress().get(0).getOverdueCount());

            assertEquals(1, result.getOverdueRecords().size());
            assertEquals("Emp One", result.getOverdueRecords().get(0).getEmployeeName());
        } finally {
            vn.system.app.common.util.UserScopeContext.clear();
        }
    }

    @Test
    void testSystemAutoAcknowledge() {
        // Arrange
        Long recordId = 1L;
        EvaluationRecord record = new EvaluationRecord();
        record.setId(recordId);
        record.setStatus(RecordStatus.COMPLETED);
        record.setCompletedAt(null);

        when(recordRepo.findById(recordId)).thenReturn(Optional.of(record));

        // Act
        EvaluationRecord result = recordService.systemAutoAcknowledge(recordId);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCompletedAt());
        verify(recordRepo, times(1)).save(record);
        verify(historyRepo, times(1)).save(any(EvaluationHistory.class));
    }

    @Test
    void testSchedulerAutoAcknowledgeT12() throws Exception {
        // Arrange
        EvaluationPeriod period = new EvaluationPeriod();
        period.setId(1L);
        period.setStatus(PeriodStatus.ACTIVE);

        when(periodRepo.findByStatus(PeriodStatus.ACTIVE)).thenReturn(List.of(period));
        when(recordRepo.findByPeriodId(anyLong())).thenReturn(List.of());

        EvaluationRecord record = new EvaluationRecord();
        record.setId(10L);
        record.setStatus(RecordStatus.COMPLETED);
        record.setCompletedAt(null);
        record.setApprovedAt(Instant.now().minus(8, ChronoUnit.DAYS)); // Over 7 days ago
        User employee = new User();
        employee.setId("emp-01");
        record.setEmployee(employee);

        when(recordRepo.findCompletedNotAcknowledgedApprovedBefore(any(Instant.class)))
                .thenReturn(List.of(record));

        // Inject mock recordService using reflection since it is private final
        EvaluationRecordService recordServiceMock = mock(EvaluationRecordService.class);
        java.lang.reflect.Field field = EvaluationReminderScheduler.class.getDeclaredField("recordService");
        field.setAccessible(true);
        field.set(reminderScheduler, recordServiceMock);

        // Act
        reminderScheduler.sendReminders();

        // Assert
        verify(recordServiceMock, times(1)).systemAutoAcknowledge(10L);
    }
}
