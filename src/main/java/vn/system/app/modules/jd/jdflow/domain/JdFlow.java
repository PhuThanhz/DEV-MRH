package vn.system.app.modules.jd.jdflow.domain;

import java.time.Instant;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.jd.jobdescription.domain.JobDescription;
import vn.system.app.modules.user.domain.User;

@Entity
@Table(name = "jd_flow")
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class JdFlow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * ==========================
     * JD
     * ==========================
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jd_id", nullable = false, unique = true)
    private JobDescription jobDescription;

    /*
     * ==========================
     * NGƯỜI GỬI JD
     * ==========================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id")
    private User fromUser;

    /*
     * ==========================
     * NGƯỜI ĐANG XỬ LÝ
     * ==========================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_user_id")
    private User currentUser;

    /*
     * ==========================
     * STATUS FLOW
     * ==========================
     */
    @Column(length = 20)
    private String status;

    /*
     * ==========================
     * AUDIT
     * ==========================
     */
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