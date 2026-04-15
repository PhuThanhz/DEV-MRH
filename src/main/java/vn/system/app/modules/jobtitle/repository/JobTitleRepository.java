package vn.system.app.modules.jobtitle.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.jobtitle.domain.JobTitle;

@Repository
public interface JobTitleRepository
        extends JpaRepository<JobTitle, Long>, JpaSpecificationExecutor<JobTitle> {

    boolean existsByNameVi(String nameVi);

    List<JobTitle> findByActiveTrue();

    List<JobTitle> findAllByOrderByPositionLevel_BandOrderDesc();

    boolean existsByNameViAndPositionLevel_Id(String nameVi, Long positionLevelId);

    boolean existsByNameViAndPositionLevel_IdAndIdNot(String nameVi, Long positionLevelId, Long id);

    // ✅ THÊM — lấy JobTitle active theo company (qua PositionLevel)
    List<JobTitle> findByPositionLevel_Company_IdAndActiveTrue(Long companyId);
}