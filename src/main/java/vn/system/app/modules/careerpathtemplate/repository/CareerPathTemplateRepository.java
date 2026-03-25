package vn.system.app.modules.careerpathtemplate.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.careerpathtemplate.domain.CareerPathTemplate;

@Repository
public interface CareerPathTemplateRepository
        extends JpaRepository<CareerPathTemplate, Long> {

    // Lấy tất cả active, sort theo tên
    List<CareerPathTemplate> findByActiveTrueOrderByNameAsc();

    // Lấy active theo phòng ban — dùng khi HR assign nhân viên
    List<CareerPathTemplate> findByDepartment_IdAndActiveTrueOrderByNameAsc(Long departmentId);

    // Lấy tất cả theo phòng ban (kể cả inactive — cho admin)
    List<CareerPathTemplate> findByDepartment_IdOrderByNameAsc(Long departmentId);

    // Check trùng tên trong cùng phòng ban
    boolean existsByNameAndDepartment_Id(String name, Long departmentId);

    // Check trùng tên trong cùng phòng ban — bỏ qua chính nó khi update
    boolean existsByNameAndDepartment_IdAndIdNot(String name, Long departmentId, Long id);
}