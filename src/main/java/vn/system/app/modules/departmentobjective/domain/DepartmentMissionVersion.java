package vn.system.app.modules.departmentobjective.domain;

import java.time.Instant;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.department.domain.Department;

@Entity
@Table(name = "department_mission_versions")
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class DepartmentMissionVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @JsonIgnoreProperties({ "company" })
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id")
    private DepartmentMission mission;

    private Integer version;

    private String title;

    @Column(length = 1000)
    private String changeSummary;

    private LocalDate effectiveDate;

    private LocalDate issueDate;

    private Long objectiveCount;

    private Long taskCount;

    private Long authorityCount;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String snapshotJson;

    private String createdBy;

    private Instant createdAt;

    @PrePersist
    public void beforeCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}
