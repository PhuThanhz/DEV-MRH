package vn.system.app.modules.documentfolder.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import vn.system.app.common.util.SecurityUtil;

@Entity
@Table(
        name = "document_folders",
        indexes = @Index(
                name = "idx_document_folders_document_category",
                columnList = "document_category_id"))
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class DocumentFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "folder_name", nullable = false)
    private String folderName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties({ "children" })
    private DocumentFolder parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({ "parent" })
    private List<DocumentFolder> children = new ArrayList<>();

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(name = "folder_type")
    private String folderType = "PERSONAL"; // ACCOUNTING, PERSONAL

    @Column(name = "company_id")
    private Long companyId;

    /**
     * Danh mục loại văn bản dùng để sinh thư mục hệ thống trong kho cá nhân.
     *
     * Chỉ lưu id thay vì tạo khóa ngoại để việc xóa danh mục không làm mất hoặc
     * khóa các thư mục cũ đang chứa dữ liệu. Giá trị null nghĩa là thư mục do
     * người dùng tự tạo (hoặc thư mục legacy đã được giữ lại).
     */
    @Column(name = "document_category_id")
    private Long documentCategoryId;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @org.hibernate.annotations.Formula("(SELECT COUNT(d.id) FROM document d WHERE d.folder_id = id AND d.active = true)")
    private Long documentCount;

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
