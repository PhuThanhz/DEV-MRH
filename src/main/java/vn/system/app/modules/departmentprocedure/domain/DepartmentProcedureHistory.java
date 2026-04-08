package vn.system.app.modules.departmentprocedure.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "department_procedure_history")
@Getter
@Setter
public class DepartmentProcedureHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_id", nullable = false)
    private DepartmentProcedure procedure;

    // ===== SNAPSHOT =====
    private Integer version;
    private String procedureCode; // ← THÊM MỚI
    private String procedureName;
    private String status;
    private Integer planYear;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String fileUrls; // ← đổi từ fileUrl

    private String note;

    // lưu tên để tránh mất data nếu sau này department/section bị xoá
    private String departmentName;
    private String sectionName;

    // ===== AUDIT =====
    private String action; // "EDIT" hoặc "REVISE" ← THÊM
    private Instant changedAt;
    private String changedBy;
}