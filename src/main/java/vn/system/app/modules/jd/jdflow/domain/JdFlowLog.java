package vn.system.app.modules.jd.jdflow.domain;

import java.time.Instant;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.modules.jd.jobdescription.domain.JobDescription;
import vn.system.app.modules.user.domain.User;

@Entity
@Table(name = "jd_flow_logs")
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class JdFlowLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * ==========================
     * JD
     * ==========================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jd_id", nullable = false)
    private JobDescription jobDescription;

    /*
     * ==========================
     * NGƯỜI GỬI
     * ==========================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id")
    private User fromUser;

    /*
     * ==========================
     * NGƯỜI NHẬN
     * ==========================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id")
    private User toUser;

    /*
     * ==========================
     * ACTION
     * ==========================
     */
    @Column(length = 30)
    private String action;

    /*
     * ==========================
     * COMMENT
     * ==========================
     */
    @Column(columnDefinition = "TEXT")
    private String comment;

    /*
     * ==========================
     * TIME
     * ==========================
     */
    private Instant createdAt;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
    }
}