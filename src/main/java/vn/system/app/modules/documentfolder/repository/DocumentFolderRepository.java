package vn.system.app.modules.documentfolder.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.system.app.modules.documentfolder.domain.DocumentFolder;

@Repository
public interface DocumentFolderRepository extends
        JpaRepository<DocumentFolder, Long>,
        JpaSpecificationExecutor<DocumentFolder> {

    List<DocumentFolder> findByOwnerIdAndParentIsNull(String ownerId);

    List<DocumentFolder> findByOwnerId(String ownerId);

    boolean existsByOwnerIdAndParentIdAndFolderName(String ownerId, Long parentId, String folderName);

    boolean existsByOwnerIdAndParentIsNullAndFolderName(String ownerId, String folderName);

    boolean existsByOwnerIdAndParentIdAndFolderNameAndIdNot(String ownerId, Long parentId, String folderName, Long id);

    boolean existsByOwnerIdAndParentIsNullAndFolderNameAndIdNot(String ownerId, String folderName, Long id);

    // ACCOUNTING FOLDERS
    List<DocumentFolder> findByFolderTypeAndCompanyIdAndParentIsNull(String folderType, Long companyId);

    List<DocumentFolder> findByFolderTypeAndCompanyId(String folderType, Long companyId);

    boolean existsByFolderTypeAndCompanyIdAndParentIdAndFolderName(String folderType, Long companyId, Long parentId, String folderName);

    boolean existsByFolderTypeAndCompanyIdAndParentIsNullAndFolderName(String folderType, Long companyId, String folderName);

    boolean existsByFolderTypeAndCompanyIdAndParentIdAndFolderNameAndIdNot(String folderType, Long companyId, Long parentId, String folderName, Long id);

    boolean existsByFolderTypeAndCompanyIdAndParentIsNullAndFolderNameAndIdNot(String folderType, Long companyId, String folderName, Long id);
}
