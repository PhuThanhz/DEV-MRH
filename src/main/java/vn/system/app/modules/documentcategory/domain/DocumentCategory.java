package vn.system.app.modules.documentcategory.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import vn.system.app.common.util.SecurityUtil;

@Entity
@Table(name = "document_category")
@Getter
@Setter
public class DocumentCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_code", length = 50, nullable = false, unique = true)
    private String categoryCode;

    @Column(name = "category_name", length = 200, nullable = false)
    private String categoryName;

    @Column(name = "symbol", length = 20)
    private String symbol;

    @Column(name = "definition", columnDefinition = "TEXT")
    private String definition;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "mapping_procedure", nullable = false)
    private boolean mappingProcedure = false;

    @Column(name = "is_cross_company", nullable = false)
    private boolean isCrossCompany = false;

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
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}