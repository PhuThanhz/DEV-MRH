package vn.system.app.modules.section.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.section.domain.Section;

@Repository
public interface SectionRepository
        extends JpaRepository<Section, Long>, JpaSpecificationExecutor<Section> {

    boolean existsByCode(String code);

    // ⭐ ĐÚNG CHUẨN thay cho UniqueConstraint
    boolean existsByCodeAndDepartmentId(String code, Long departmentId);

    Optional<Section> findByCode(String code);
}
