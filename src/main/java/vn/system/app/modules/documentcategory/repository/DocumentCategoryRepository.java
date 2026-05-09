package vn.system.app.modules.documentcategory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.documentcategory.domain.DocumentCategory;

@Repository
public interface DocumentCategoryRepository extends
        JpaRepository<DocumentCategory, Long>,
        JpaSpecificationExecutor<DocumentCategory> {

    boolean existsByCategoryCode(String categoryCode);

    boolean existsByCategoryCodeAndIdNot(String categoryCode, Long id);

    List<DocumentCategory> findByActiveTrue();

    List<DocumentCategory> findByMappingProcedureTrue();
}