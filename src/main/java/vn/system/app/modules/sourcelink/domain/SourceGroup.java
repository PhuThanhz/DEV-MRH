package vn.system.app.modules.sourcelink.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_group_id")
    @JsonIgnoreProperties("groups")
    private SourceGroupMain mainGroup;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("group")
    private List<SourceLink> links = new ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    public void addLink(SourceLink link) {
        link.setGroup(this);
        links.add(link);
    }

    public void removeLink(SourceLink link) {
        links.remove(link);
        link.setGroup(null);
    }

    @PrePersist
    public void onCreate() {
        createdAt = Instant.now();
        createdBy = SecurityUtil.getCurrentUserLogin().orElse("system");
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
        updatedBy = SecurityUtil.getCurrentUserLogin().orElse("system");
    }
}
