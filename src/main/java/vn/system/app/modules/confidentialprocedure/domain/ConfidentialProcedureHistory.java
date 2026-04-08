package vn.system.app.modules.confidentialprocedure.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "confidential_procedure_history")
@Getter
@Setter
public class ConfidentialProcedureHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_id", nullable = false)
    private ConfidentialProcedure procedure;

    private Integer version;
    private String procedureCode; // ← THÊM MỚI
    private String procedureName;
    private String status;
    private Integer planYear;
    private Instant issuedDate; // ← THÊM

    @Column(columnDefinition = "MEDIUMTEXT")
    private String fileUrls;

    private String note;
    private String departmentName;
    private String sectionName;

    private String action; // "EDIT" hoặc "REVISE"
    private Instant changedAt;
    private String changedBy;
}