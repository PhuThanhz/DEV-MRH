package vn.system.app.modules.companyprocedure.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.companyprocedure.domain.enums.ProcedureStatus;
import vn.system.app.modules.section.domain.Section;

@Entity
@Table(name = "company_procedures", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "section_id", "procedure_name" })
})
@Getter
@Setter
public class CompanyProcedure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tên quy trình / quy định
    @Column(name = "procedure_name", nullable = false)
    private String procedureName;

    // Link file PDF / URL
    @Column(length = 500)
    private String fileUrl;

    // Bộ phận / team phụ trách
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    /*
     * Trạng thái quy trình:
     * NEED_CREATE : Cần xây dựng mới
     * IN_PROGRESS : Đang xây dựng
     * NEED_UPDATE : Cần cập nhật
     * TERMINATED : Chấm dứt
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcedureStatus status;

    // Kế hoạch năm (ví dụ: 2026)
    private Integer planYear;

    // Ghi chú
    @Column(columnDefinition = "TEXT")
    private String note;

    // Audit
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
