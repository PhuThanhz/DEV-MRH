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

    /*
     * ==========================================
     * JD DO USER TẠO
     * ==========================================
     */
    List<JobDescription> findByCreatedBy(String createdBy);

    /*
     * ==========================================
     * JD THEO STATUS
     * ==========================================
     */
    List<JobDescription> findByStatus(String status);

    /*
     * ==========================================
     * JD THEO DEPARTMENT
     * ==========================================
     */
    List<JobDescription> findByDepartmentId(Long departmentId);

    /*
     * ==========================================
     * JD BỊ TỪ CHỐI CỦA USER
     * ==========================================
     */
    List<JobDescription> findByCreatedByAndStatus(String createdBy, String status);

    Page<JobDescription> findByCreatedBy(String createdBy, Pageable pageable);

}