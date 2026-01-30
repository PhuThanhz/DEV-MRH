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

    boolean existsByJobTitle_IdAndDepartment_Id(Long jobTitleId, Long departmentId);

    List<DepartmentJobTitle> findByDepartment_Id(Long departmentId);
}
