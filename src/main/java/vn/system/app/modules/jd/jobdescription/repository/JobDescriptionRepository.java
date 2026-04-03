package vn.system.app.modules.jd.jobdescription.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.system.app.modules.jd.jobdescription.domain.JobDescription;

@Repository
public interface JobDescriptionRepository
        extends JpaRepository<JobDescription, Long>,
        JpaSpecificationExecutor<JobDescription> {

    List<JobDescription> findByCreatedBy(String createdBy);

    List<JobDescription> findByStatus(String status);

    List<JobDescription> findByDepartmentId(Long departmentId);

    List<JobDescription> findByCreatedByAndStatus(String createdBy, String status);

    Page<JobDescription> findByCreatedBy(String createdBy, Pageable pageable);

    // ← THÊM 2 CÁI NÀY
    Page<JobDescription> findByStatus(String status, Pageable pageable);

    Page<JobDescription> findAll(Pageable pageable); // đã có sẵn từ JpaRepository, không cần thêm
}