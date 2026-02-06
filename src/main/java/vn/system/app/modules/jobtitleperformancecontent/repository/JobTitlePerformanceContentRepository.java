package vn.system.app.modules.jobtitleperformancecontent.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import vn.system.app.modules.jobtitleperformancecontent.domain.JobTitlePerformanceContent;
import vn.system.app.modules.jobtitleperformancecontent.domain.OwnerLevel;

@Repository
public interface JobTitlePerformanceContentRepository
        extends JpaRepository<JobTitlePerformanceContent, Long>,
        JpaSpecificationExecutor<JobTitlePerformanceContent> {

    boolean existsByOwnerLevelAndOwnerJobTitleIdAndSalaryGradeId(
            OwnerLevel ownerLevel,
            Long ownerJobTitleId,
            Long salaryGradeId);

    /** KIỂM TRA TRÙNG THEO BẬC LƯƠNG + CHỈ ACTIVE */
    boolean existsByOwnerJobTitleIdAndSalaryGradeNumberAndActiveTrue(
            Long ownerJobTitleId,
            Integer salaryGradeNumber);

    /** LẤY DS ACTIVE */
    List<JobTitlePerformanceContent> findByOwnerLevelAndOwnerJobTitleIdAndActiveTrue(
            OwnerLevel ownerLevel,
            Long ownerJobTitleId);

    /** UPDATE ACTIVE */
    @Modifying
    @Query("""
                UPDATE JobTitlePerformanceContent e
                   SET e.active = :active
                 WHERE e.id = :id
            """)
    void updateActive(@Param("id") Long id, @Param("active") Boolean active);
}
