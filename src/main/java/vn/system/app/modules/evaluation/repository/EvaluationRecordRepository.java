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

    /** Danh sách form nhân viên mà quản lý trực tiếp cần chấm */
    List<EvaluationRecord> findByDirectManagerIdAndStatus(String directManagerId, RecordStatus status);

    /** Tất cả form của quản lý trực tiếp trong kỳ */
    List<EvaluationRecord> findByPeriodIdAndDirectManagerId(Long periodId, String directManagerId);

    /** Danh sách form mà quản lý gián tiếp cần phê duyệt */
    List<EvaluationRecord> findByIndirectManagerIdAndStatus(String indirectManagerId, RecordStatus status);

    /** Tất cả form của quản lý gián tiếp trong kỳ */
    List<EvaluationRecord> findByPeriodIdAndIndirectManagerId(Long periodId, String indirectManagerId);

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

    /** Đếm theo trạng thái trong kỳ — dùng cho dashboard báo cáo */
    @Query("SELECT r.status, COUNT(r) FROM EvaluationRecord r WHERE r.period.id = :periodId GROUP BY r.status")
    List<Object[]> countByPeriodGroupByStatus(@Param("periodId") Long periodId);

    /** Đếm theo xếp loại trong kỳ — dùng cho biểu đồ phân bổ A/B/C/D/E */
    @Query("SELECT r.finalGrade, COUNT(r) FROM EvaluationRecord r WHERE r.period.id = :periodId AND r.status = 'COMPLETED' GROUP BY r.finalGrade")
    List<Object[]> countByPeriodGroupByFinalGrade(@Param("periodId") Long periodId);
}
