package vn.system.app.modules.document.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.document.domain.DocumentAccess;

@Repository
public interface DocumentAccessRepository
        extends JpaRepository<DocumentAccess, Long> {

    List<DocumentAccess> findByDocument_Id(Long documentId);

    void deleteByDocument_Id(Long documentId);

    boolean existsByDocument_IdAndUserId(Long documentId, String userId);

    List<DocumentAccess> findByDocument_IdAndUserId(Long documentId, String userId);
}