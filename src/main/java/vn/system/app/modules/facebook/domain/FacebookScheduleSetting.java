package vn.system.app.modules.facebook.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.sourcelink.domain.SourceGroup;

import java.time.Instant;

@Entity
@Table(name = "facebook_schedule_setting")
@Getter
@Setter
public class FacebookScheduleSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết group nào đang cấu hình
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_group_id", nullable = false)
    private SourceGroup sourceGroup;

    // Fanpage sẽ đăng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facebook_page_id", nullable = false)
    private FacebookPage facebookPage;

    // Giờ bắt đầu đăng
    @Column(nullable = true)
    private Instant startTime;

    // Khoảng cách giữa các bài (phút)
    @Column(nullable = true)
    private Integer intervalMinutes;

    // Thứ tự link (JSON dạng [1,3,2,5])
    @Column(columnDefinition = "TEXT")
    private String orderedLinkIds;

    // Ghi chú hoặc tag lịch đăng
    private String label;

    // Audit
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
