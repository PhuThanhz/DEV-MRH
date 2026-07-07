package vn.system.app.modules.departmentobjective.domain;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.department.domain.Department;

/*
 * Audit tổng cho cả bộ mục tiêu/nhiệm vụ của MỘT phòng ban (1 bản ghi / phòng ban).
 * Giữ thông tin ban hành + cập nhật cuối ở mức tổng thể, tách khỏi audit từng dòng
 * trong department_objectives.
 */
@Entity
@Table(name = "department_missions")
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class DepartmentMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", unique = true)
    @JsonIgnoreProperties({ "company" })
    private Department department;

    /*
     * DRAFT | PUBLISHED
     */
    private String status;

    private LocalDate issueDate;

    private String issuedBy;
    private Instant issuedAt;

    private String lastUpdatedBy;
    private Instant lastUpdatedAt;

    /*
     * Số phiên bản đã ban hành (tăng mỗi lần Ban hành)
     */
    private Integer version;

    @PrePersist
    public void beforeCreate() {
        if (this.status == null) {
            this.status = "DRAFT";
        }
        if (this.version == null) {
            this.version = 0;
        }
        this.lastUpdatedAt = Instant.now();
        this.lastUpdatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
