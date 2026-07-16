package vn.system.app.modules.evaluation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.evaluation.domain.EvaluationRecord;
import vn.system.app.modules.evaluation.domain.enums.RecordStatus;

@Repository
public interface EvaluationRecordRepository extends JpaRepository<EvaluationRecord, Long> {

    Optional<EvaluationRecord> findByPeriodIdAndEmployeeId(Long periodId, String employeeId);

    List<EvaluationRecord> findByPeriodId(Long periodId);

    List<EvaluationRecord> findByPeriodIdAndStatus(Long periodId, RecordStatus status);

    List<EvaluationRecord> findByStatus(RecordStatus status);

    List<EvaluationRecord> findByPeriodIdAndStatusAndEmployeeIdIn(Long periodId, RecordStatus status, java.util.Collection<String> employeeIds);

    List<EvaluationRecord> findByStatusAndEmployeeIdIn(RecordStatus status, java.util.Collection<String> employeeIds);

    org.springframework.data.domain.Page<EvaluationRecord> findByPeriodIdAndStatusAndEmployeeIdIn(Long periodId, RecordStatus status, java.util.Collection<String> employeeIds, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<EvaluationRecord> findByPeriodIdAndStatus(Long periodId, RecordStatus status, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<EvaluationRecord> findByStatusAndEmployeeIdIn(RecordStatus status, java.util.Collection<String> employeeIds, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<EvaluationRecord> findByStatus(RecordStatus status, org.springframework.data.domain.Pageable pageable);

    @Query(value = """
        SELECT r FROM EvaluationRecord r
        WHERE r.status = 'COMPLETED'
          AND (:periodId IS NULL OR r.period.id = :periodId)
          AND (:hasEmployeeIds = false OR r.employee.id IN :employeeIds)
          AND (:finalGrade IS NULL OR r.finalGrade = :finalGrade)
          AND (:searchText IS NULL OR LOWER(r.employee.name) LIKE LOWER(CONCAT('%', :searchText, '%')) OR LOWER(r.employee.email) LIKE LOWER(CONCAT('%', :searchText, '%')))
    """)
    org.springframework.data.domain.Page<EvaluationRecord> findCompletedSummary(
        @Param("periodId") Long periodId,
        @Param("hasEmployeeIds") boolean hasEmployeeIds,
        @Param("employeeIds") java.util.Collection<String> employeeIds,
        @Param("finalGrade") String finalGrade,
        @Param("searchText") String searchText,
        org.springframework.data.domain.Pageable pageable
    );

    boolean existsByTemplateId(Long templateId);

    /** Danh sách form nhân viên mà quản lý trực tiếp cần chấm */
    List<EvaluationRecord> findByDirectManagerIdAndStatusIn(String directManagerId, List<RecordStatus> statuses);

    /** Tất cả form của quản lý trực tiếp trong kỳ */
    List<EvaluationRecord> findByPeriodIdAndDirectManagerId(Long periodId, String directManagerId);

    /** Tất cả form của quản lý trực tiếp */
    List<EvaluationRecord> findByDirectManagerIdOrderByCreatedAtDesc(String directManagerId);

    /** Danh sách form mà quản lý gián tiếp cần phê duyệt */
    List<EvaluationRecord> findByIndirectManagerIdAndStatusIn(String indirectManagerId, List<RecordStatus> statuses);

    /** Tất cả form của quản lý gián tiếp trong kỳ */
    List<EvaluationRecord> findByPeriodIdAndIndirectManagerId(Long periodId, String indirectManagerId);

    /** Tất cả form của quản lý gián tiếp (lịch sử) */
    List<EvaluationRecord> findByIndirectManagerIdOrderByCreatedAtDesc(String indirectManagerId);

    /** Các kỳ mà nhân viên tham gia (lịch sử) */
    List<EvaluationRecord> findByEmployeeIdOrderByCreatedAtDesc(String employeeId);

    /**
     * Fetch record kèm template + sections + criteria (không fetch levels ở đây
     * để tránh MultipleBagFetchException — levels sẽ được lazy load trong
     * transaction)
     */
    @Query("""
                SELECT DISTINCT r FROM EvaluationRecord r
                LEFT JOIN FETCH r.template t
                LEFT JOIN FETCH t.sections s
                LEFT JOIN FETCH s.criteria c
                WHERE r.id = :id
            """)
    Optional<EvaluationRecord> findByIdWithFullTemplate(@Param("id") Long id);

    long countByPeriodIdAndStatusNotIn(Long periodId, java.util.Collection<RecordStatus> statuses);

    List<EvaluationRecord> findByPeriodIdAndStatusNotIn(Long periodId, java.util.Collection<RecordStatus> statuses);

    /** Đếm theo trạng thái trong kỳ — dùng cho dashboard báo cáo */
    @Query("SELECT r.status, COUNT(r) FROM EvaluationRecord r WHERE r.period.id = :periodId AND r.status <> 'CANCELLED' GROUP BY r.status")
    List<Object[]> countByPeriodGroupByStatus(@Param("periodId") Long periodId);

    /** Đếm theo xếp loại trong kỳ — dùng cho biểu đồ phân bổ A/B/C/D/E */
    @Query("SELECT r.finalGrade, COUNT(r) FROM EvaluationRecord r WHERE r.period.id = :periodId AND r.status = 'COMPLETED' GROUP BY r.finalGrade")
    List<Object[]> countByPeriodGroupByFinalGrade(@Param("periodId") Long periodId);

    /**
     * T12 — Tìm record đã COMPLETED nhưng chưa được nhân viên xác nhận (completedAt = null),
     * và approvedAt trước một mốc thời gian nhất định.
     * Dùng để: (1) gửi nhắc nhở định kỳ, (2) auto-acknowledge sau N ngày.
     */
    @Query("""
        SELECT r FROM EvaluationRecord r
        WHERE r.status = 'COMPLETED'
          AND r.completedAt IS NULL
          AND r.approvedAt IS NOT NULL
          AND r.approvedAt < :threshold
    """)
    List<EvaluationRecord> findCompletedNotAcknowledgedApprovedBefore(
        @Param("threshold") java.time.Instant threshold);
}
