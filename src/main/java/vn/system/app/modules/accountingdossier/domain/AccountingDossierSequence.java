package vn.system.app.modules.accountingdossier.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.company.domain.Company;

@Entity
@Table(name = "accounting_dossier_sequence", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "company_id", "year" })
})
@Getter
@Setter
public class AccountingDossierSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "current_number", nullable = false)
    private int currentNumber = 0;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("system");
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("system");
    }
}
