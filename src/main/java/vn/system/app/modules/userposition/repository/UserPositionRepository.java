package vn.system.app.modules.userposition.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.system.app.modules.userposition.domain.UserPosition;

public interface UserPositionRepository
        extends JpaRepository<UserPosition, Long>, JpaSpecificationExecutor<UserPosition> {

    List<UserPosition> findByUser_IdAndActiveTrue(Long userId);

    // ── COMPANY ──────────────────────────────────────────────────────────────
    boolean existsByUser_IdAndCompanyJobTitle_IdAndActiveTrue(Long userId, Long companyJobTitleId);

    Optional<UserPosition> findByUser_IdAndCompanyJobTitle_IdAndActiveFalse(
            Long userId, Long companyJobTitleId);

    // ── DEPARTMENT ────────────────────────────────────────────────────────────
    boolean existsByUser_IdAndDepartmentJobTitle_IdAndActiveTrue(Long userId, Long departmentJobTitleId);

    Optional<UserPosition> findByUser_IdAndDepartmentJobTitle_IdAndActiveTrue(
            Long userId, Long departmentJobTitleId);

    Optional<UserPosition> findByUser_IdAndDepartmentJobTitle_IdAndActiveFalse(
            Long userId, Long departmentJobTitleId);

    // ── SECTION ───────────────────────────────────────────────────────────────
    boolean existsByUser_IdAndSectionJobTitle_IdAndActiveTrue(Long userId, Long sectionJobTitleId);

    Optional<UserPosition> findByUser_IdAndSectionJobTitle_IdAndActiveFalse(
            Long userId, Long sectionJobTitleId);

    // ── MISC ──────────────────────────────────────────────────────────────────
    Optional<UserPosition> findByUser_IdAndSourceAndActiveTrue(Long userId, String source);

    // ── QUERIES ───────────────────────────────────────────────────────────────
    @Query("""
                SELECT up FROM UserPosition up
                LEFT JOIN up.companyJobTitle cjt
                LEFT JOIN cjt.company comp1
                LEFT JOIN up.departmentJobTitle djt
                LEFT JOIN djt.department dept
                LEFT JOIN dept.company comp2
                LEFT JOIN up.sectionJobTitle sjt
                LEFT JOIN sjt.section sec
                LEFT JOIN sec.department sdept
                LEFT JOIN sdept.company comp3
                WHERE up.active = true
                AND (
                    comp1.id = :companyId
                    OR comp2.id = :companyId
                    OR comp3.id = :companyId
                )
            """)
    List<UserPosition> findActiveByCompanyId(@Param("companyId") Long companyId);

    @Query("""
                SELECT up FROM UserPosition up
                LEFT JOIN FETCH up.companyJobTitle cjt
                LEFT JOIN FETCH cjt.company

                LEFT JOIN FETCH up.departmentJobTitle djt
                LEFT JOIN FETCH djt.department dept
                LEFT JOIN FETCH dept.company

                LEFT JOIN FETCH up.sectionJobTitle sjt
                LEFT JOIN FETCH sjt.section sec
                LEFT JOIN FETCH sec.department sdept
                LEFT JOIN FETCH sdept.company

                WHERE up.user.id = :userId
                AND up.active = true
            """)
    List<UserPosition> findActiveFullByUserId(@Param("userId") Long userId);

    @Query("""
                SELECT CASE WHEN COUNT(up) > 0 THEN true ELSE false END
                FROM UserPosition up
                LEFT JOIN up.companyJobTitle cjt
                LEFT JOIN up.departmentJobTitle djt
                LEFT JOIN up.sectionJobTitle sjt
                WHERE up.active = true
                AND (
                    cjt.jobTitle.id = :jobTitleId OR
                    djt.jobTitle.id = :jobTitleId OR
                    sjt.jobTitle.id = :jobTitleId
                )
            """)
    boolean existsByJobTitleIdAndActiveTrue(@Param("jobTitleId") Long jobTitleId);

    @Query("""
                SELECT DISTINCT up.user.id FROM UserPosition up
                LEFT JOIN up.departmentJobTitle djt
                LEFT JOIN up.sectionJobTitle sjt
                LEFT JOIN sjt.section s
                WHERE up.active = true
                AND (
                    (up.source = 'DEPARTMENT' AND djt.department.id = :departmentId)
                    OR
                    (up.source = 'SECTION' AND s.department.id = :departmentId)
                )
            """)
    List<Long> findUserIdsByDepartmentId(@Param("departmentId") Long departmentId);
}