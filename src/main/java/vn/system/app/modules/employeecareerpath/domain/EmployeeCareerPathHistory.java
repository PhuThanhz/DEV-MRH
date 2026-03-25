package vn.system.app.modules.employeecareerpath.domain;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.careerpath.domain.CareerPath;

@Entity
@Table(name = "employee_career_path_histories")
@Getter
@Setter
public class EmployeeCareerPathHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_career_path_id", nullable = false)
    private EmployeeCareerPath employeeCareerPath;

    // Bước trước
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_career_path_id")
    private CareerPath fromCareerPath;

    // Bước mới sau thăng tiến
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_career_path_id")
    private CareerPath toCareerPath;

    // Thứ tự bước trong template (để dễ tra cứu)
    private Integer fromStepOrder;
    private Integer toStepOrder;

    private LocalDate promotedAt;

    @Column(columnDefinition = "TEXT")
    private String note;

    private Instant createdAt;
    private String createdBy;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}