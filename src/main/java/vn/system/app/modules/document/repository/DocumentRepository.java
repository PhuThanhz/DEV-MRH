package vn.system.app.modules.document.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.document.domain.Document;

@Repository
public interface DocumentRepository extends
        JpaRepository<Document, Long>,
        JpaSpecificationExecutor<Document> {

    boolean existsByDocumentCode(String documentCode);

    boolean existsByDocumentCodeAndIdNot(String documentCode, Long id);

    @Query("SELECT d FROM Document d WHERE d.department.company.id = :companyId")
    List<Document> findByCompanyId(@Param("companyId") Long companyId);

    List<Document> findByDepartment_Id(Long departmentId);

    List<Document> findBySection_Id(Long sectionId);

    List<Document> findByCategory_Id(Long categoryId);

    boolean existsByCategory_Id(Long categoryId);

    Optional<Document> findByQrToken(String qrToken);
}