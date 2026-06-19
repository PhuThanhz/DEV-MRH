package vn.system.app.modules.document.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.document.domain.DocumentShortcut;

@Repository
public interface DocumentShortcutRepository extends JpaRepository<DocumentShortcut, Long> {

    List<DocumentShortcut> findByFolderId(Long folderId);

    boolean existsByDocumentIdAndFolderId(Long documentId, Long folderId);

    void deleteByDocumentIdAndFolderId(Long documentId, Long folderId);
}
