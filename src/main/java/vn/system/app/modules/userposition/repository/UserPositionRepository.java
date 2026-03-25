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

    boolean existsByUser_IdAndCompanyJobTitle_IdAndActiveTrue(Long userId, Long companyJobTitleId);

    boolean existsByUser_IdAndDepartmentJobTitle_IdAndActiveTrue(Long userId, Long departmentJobTitleId);

    boolean existsByUser_IdAndSectionJobTitle_IdAndActiveTrue(Long userId, Long sectionJobTitleId);

    // ✅ THÊM — dùng khi promote: tìm UserPosition DEPARTMENT active của user
    Optional<UserPosition> findByUser_IdAndSourceAndActiveTrue(Long userId, String source);

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
}