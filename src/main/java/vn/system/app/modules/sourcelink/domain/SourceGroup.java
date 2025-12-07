package vn.system.app.modules.sourcelink.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "source_groups")
@Getter
@Setter
public class SourceGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SourceLink> links = new ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    public void addLink(SourceLink link) {
        link.setGroup(this);
        this.links.add(link);
    }

    public void removeLink(SourceLink link) {
        this.links.remove(link);
        link.setGroup(null);
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("system");
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("system");
    }
}
