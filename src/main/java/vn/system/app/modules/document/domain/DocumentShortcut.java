package vn.system.app.modules.document.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.documentfolder.domain.DocumentFolder;

@Entity
@Table(name = "document_shortcuts")
@Getter
@Setter
public class DocumentShortcut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = false)
    private DocumentFolder folder;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "created_at")
    private Instant createdAt;
}
