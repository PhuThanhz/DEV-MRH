package vn.system.app.modules.accountingdossier.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.accountingdossier.domain.enums.WorkflowTemplateStatus;

@Entity
@Table(name = "accounting_approval_workflow_templates", indexes = {
        @Index(name = "idx_acc_wf_template_company_status", columnList = "company_id,status"),
        @Index(name = "idx_acc_wf_template_category", columnList = "dossier_category_id"),
        @Index(name = "idx_acc_wf_template_priority", columnList = "priority")
})
@Getter
@Setter
public class AccountingApprovalWorkflowTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "dossier_category_id")
    private Long dossierCategoryId;

    @Column(name = "business_type", length = 80)
    private String businessType;

    @Column(nullable = false)
    private Integer priority = 100;

    @Column(name = "is_default", nullable = false)
    private boolean defaultTemplate = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WorkflowTemplateStatus status = WorkflowTemplateStatus.DRAFT;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(name = "effective_from")
    private Instant effectiveFrom;

    @Column(name = "effective_to")
    private Instant effectiveTo;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountingApprovalWorkflowStep> steps = new ArrayList<>();

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountingApprovalWorkflowScope> scopes = new ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
        if (this.status == null) {
            this.status = WorkflowTemplateStatus.DRAFT;
        }
        if (this.version == null) {
            this.version = 1;
        }
        if (this.priority == null) {
            this.priority = 100;
        }
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
