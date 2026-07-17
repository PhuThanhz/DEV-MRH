package vn.system.app.modules.jobtitle.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.jobtitle.domain.JobTitle;

@Repository
public interface JobTitleRepository
        extends JpaRepository<JobTitle, Long>, JpaSpecificationExecutor<JobTitle> {

    @Override
    @EntityGraph(attributePaths = { "positionLevel", "positionLevel.company" })
    Page<JobTitle> findAll(Specification<JobTitle> spec, Pageable pageable);

    boolean existsByNameVi(String nameVi);

    @EntityGraph(attributePaths = { "positionLevel", "positionLevel.company" })
    List<JobTitle> findByActiveTrue();

    List<JobTitle> findAllByOrderByPositionLevel_BandOrderDesc();

    boolean existsByNameViAndPositionLevel_Id(String nameVi, Long positionLevelId);

    boolean existsByNameViAndPositionLevel_IdAndIdNot(String nameVi, Long positionLevelId, Long id);

    // ✅ THÊM — lấy JobTitle active theo company (qua PositionLevel)
    @EntityGraph(attributePaths = { "positionLevel", "positionLevel.company" })
    List<JobTitle> findByPositionLevel_Company_IdAndActiveTrue(Long companyId);
}
