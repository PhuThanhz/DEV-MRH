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

    // kiểm tra code trùng trong cùng phòng ban
    boolean existsByCodeAndDepartmentId(String code, Long departmentId);

    Optional<Section> findByCode(String code);

    // ⭐ lấy danh sách bộ phận theo phòng ban
    List<Section> findByDepartmentId(Long departmentId);
}