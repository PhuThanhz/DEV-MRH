package vn.system.app.modules.confidentialprocedure.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.section.domain.Section;

@Entity
@Table(name = "confidential_procedures")
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ConfidentialProcedure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String procedureName;
    private String status;
    private Integer planYear;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String fileUrls; // ← đổi từ fileUrl

    private String note;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Integer version = 1;

    // ===== RELATIONSHIP =====

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    @JsonIgnoreProperties({ "confidentialProcedures" })
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    @JsonIgnoreProperties({ "confidentialProcedures" })
    private Section section;

    // Danh sách user/role được gán quyền xem
    @OneToMany(mappedBy = "procedure", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConfidentialProcedureAccess> accessList = new ArrayList<>();

    // ===== AUDIT =====

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
        if (!this.active)
            this.active = true;
        if (this.version == null)
            this.version = 1;
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}