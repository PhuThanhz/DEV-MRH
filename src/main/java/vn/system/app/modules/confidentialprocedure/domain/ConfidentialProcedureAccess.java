package vn.system.app.modules.confidentialprocedure.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "confidential_procedure_access")
@Getter
@Setter
public class ConfidentialProcedureAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_id", nullable = false)
    private ConfidentialProcedure procedure;

    // Gán theo User (nullable)
    @Column(name = "user_id")
    private Long userId;

    // Gán theo Role (nullable)
    @Column(name = "role_id")
    private Long roleId;

    // "USER" hoặc "ROLE"
    @Column(nullable = false)
    private String accessType;
}