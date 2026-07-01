package vn.system.app.modules.adminscope.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.system.app.modules.adminscope.domain.UserAdminScope;

public interface UserAdminScopeRepository extends JpaRepository<UserAdminScope, Long> {

    @Query("""
            SELECT s FROM UserAdminScope s
            LEFT JOIN FETCH s.company
            LEFT JOIN FETCH s.department
            WHERE s.user.id = :userId
            AND s.active = true
            """)
    List<UserAdminScope> findByUser_IdAndActiveTrue(@Param("userId") String userId);

    Optional<UserAdminScope> findByUser_IdAndScopeTypeAndCompany_IdAndDepartment_Id(
            String userId,
            String scopeType,
            Long companyId,
            Long departmentId);

    Optional<UserAdminScope> findByUser_IdAndScopeTypeAndCompany_IdAndDepartmentIsNull(
            String userId,
            String scopeType,
            Long companyId);

    @Query("""
            SELECT DISTINCT s.company.id
            FROM UserAdminScope s
            WHERE s.user.id = :userId
            AND s.active = true
            AND s.scopeType = 'COMPANY'
            AND s.company IS NOT NULL
            """)
    List<Long> findActiveCompanyScopeIdsByUserId(@Param("userId") String userId);

    @Query("""
            SELECT DISTINCT s.department.id
            FROM UserAdminScope s
            WHERE s.user.id = :userId
            AND s.active = true
            AND s.scopeType = 'DEPARTMENT'
            AND s.department IS NOT NULL
            """)
    List<Long> findActiveDepartmentScopeIdsByUserId(@Param("userId") String userId);

    @Query("""
            SELECT DISTINCT s.company.id
            FROM UserAdminScope s
            WHERE s.user.id = :userId
            AND s.active = true
            AND s.scopeType = 'DEPARTMENT'
            AND s.company IS NOT NULL
            """)
    List<Long> findCompanyIdsFromDepartmentScopesByUserId(@Param("userId") String userId);

    @Query("""
            SELECT s FROM UserAdminScope s
            WHERE s.user.id = :userId
            AND s.active = true
            AND s.scopeType = :scopeType
            """)
    List<UserAdminScope> findActiveByUserAndScopeType(
            @Param("userId") String userId,
            @Param("scopeType") String scopeType);

    @Query("""
            SELECT DISTINCT s.company.id
            FROM UserAdminScope s
            WHERE s.user.id = :userId
            AND s.active = true
            AND s.scopeType = 'COMPANY'
            AND s.company.id IN :companyIds
            """)
    List<Long> findAllowedCompanyIds(
            @Param("userId") String userId,
            @Param("companyIds") Collection<Long> companyIds);
}
