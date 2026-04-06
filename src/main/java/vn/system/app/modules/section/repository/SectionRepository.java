package vn.system.app.modules.section.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.section.domain.Section;

@Repository
public interface SectionRepository
        extends JpaRepository<Section, Long>, JpaSpecificationExecutor<Section> {

    boolean existsByCode(String code);

    boolean existsByCodeAndDepartmentId(String code, Long departmentId);

    Optional<Section> findByCode(String code);

    List<Section> findByDepartmentId(Long departmentId);

    // THÊM MỚI — check phòng ban có bộ phận không
    boolean existsByDepartmentId(Long departmentId);

    long countByDepartment_Company_IdIn(java.util.Set<Long> companyIds);

}