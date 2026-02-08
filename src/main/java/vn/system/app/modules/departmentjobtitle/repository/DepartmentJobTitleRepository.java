package vn.system.app.modules.departmentjobtitle.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.departmentjobtitle.domain.DepartmentJobTitle;

@Repository
public interface DepartmentJobTitleRepository
        extends JpaRepository<DepartmentJobTitle, Long>,
        JpaSpecificationExecutor<DepartmentJobTitle> {

    // 🔹 Cũ — vẫn cần
    boolean existsByJobTitle_IdAndDepartment_Id(Long jobTitleId, Long departmentId);

    // 🔹 Mới — dùng cho validate chính xác (deptId đứng trước jobId)
    boolean existsByDepartment_IdAndJobTitle_Id(Long departmentId, Long jobTitleId);

    // 🔹 Mới — dùng validate: job title ACTIVE trong phòng ban?
    boolean existsByDepartment_IdAndJobTitle_IdAndActiveTrue(Long departmentId, Long jobTitleId);

    // 🔹 Find mapping theo phòng ban & chức danh
    DepartmentJobTitle findByDepartment_IdAndJobTitle_Id(Long departmentId, Long jobTitleId);

    // 🔹 Lấy danh sách theo phòng ban
    List<DepartmentJobTitle> findByDepartment_Id(Long departmentId);

    // 🔹 Lấy danh sách ACTIVE theo phòng ban
    List<DepartmentJobTitle> findByDepartment_IdAndActiveTrue(Long departmentId);

    // ⭐ NEW — COMPANY GET (FIX LỖI)
    List<DepartmentJobTitle> findByDepartment_Company_IdAndActiveTrue(Long companyId);

    // ⭐ NEW — kiểm tra JobTitle đã active ở phòng ban thuộc công ty hay chưa
    boolean existsByDepartment_Company_IdAndJobTitle_IdAndActiveTrue(
            Long companyId,
            Long jobTitleId);

    List<DepartmentJobTitle> findByJobTitle_IdAndActiveTrue(Long jobTitleId);

}
