package vn.system.app.modules.adminscope.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.system.app.modules.adminscope.domain.UserAdminScope;

public interface UserAdminScopeRepository extends JpaRepository<UserAdminScope, Long> {

        @Query(value = """
                        SELECT s.*
                        FROM user_admin_scopes s
                        WHERE s.user_id = :userId
                        AND s.active = 1
                        """, nativeQuery = true)
        List<UserAdminScope> findByUser_IdAndActiveTrue(@Param("userId") String userId);

        @Query(value = """
                        SELECT s.*
                        FROM user_admin_scopes s
                        WHERE s.user_id = :userId
                        AND s.scope_type = :scopeType
                        AND s.company_id = :companyId
                        AND s.department_id = :departmentId
                        LIMIT 1
                        """, nativeQuery = true)
        Optional<UserAdminScope> findByUser_IdAndScopeTypeAndCompany_IdAndDepartment_Id(
                        @Param("userId") String userId,
                        @Param("scopeType") String scopeType,
                        @Param("companyId") Long companyId,
                        @Param("departmentId") Long departmentId);

        @Query(value = """
                        SELECT s.*
                        FROM user_admin_scopes s
                        WHERE s.user_id = :userId
                        AND s.scope_type = :scopeType
                        AND s.company_id = :companyId
                        AND s.department_id IS NULL
                        LIMIT 1
                        """, nativeQuery = true)
        Optional<UserAdminScope> findByUser_IdAndScopeTypeAndCompany_IdAndDepartmentIsNull(
                        @Param("userId") String userId,
                        @Param("scopeType") String scopeType,
                        @Param("companyId") Long companyId);

        @Query(value = """
                        SELECT DISTINCT s.company_id
                        FROM user_admin_scopes s
                        WHERE s.user_id = :userId
                        AND s.active = 1
                        AND s.scope_type = 'COMPANY'
                        AND s.company_id IS NOT NULL
                        """, nativeQuery = true)
        List<Long> findActiveCompanyScopeIdsByUserId(@Param("userId") String userId);

        @Query(value = """
                        SELECT DISTINCT s.department_id
                        FROM user_admin_scopes s
                        WHERE s.user_id = :userId
                        AND s.active = 1
                        AND s.scope_type = 'DEPARTMENT'
                        AND s.department_id IS NOT NULL
                        """, nativeQuery = true)
        List<Long> findActiveDepartmentScopeIdsByUserId(@Param("userId") String userId);

        @Query(value = """
                        SELECT DISTINCT s.company_id
                        FROM user_admin_scopes s
                        WHERE s.user_id = :userId
                        AND s.active = 1
                        AND s.scope_type = 'DEPARTMENT'
                        AND s.company_id IS NOT NULL
                        """, nativeQuery = true)
        List<Long> findCompanyIdsFromDepartmentScopesByUserId(@Param("userId") String userId);

        @Query(value = """
                        SELECT s.*
                        FROM user_admin_scopes s
                        WHERE s.user_id = :userId
                        AND s.active = 1
                        AND s.scope_type = :scopeType
                        """, nativeQuery = true)
        List<UserAdminScope> findActiveByUserAndScopeType(
                        @Param("userId") String userId,
                        @Param("scopeType") String scopeType);

        @Query(value = """
                        SELECT DISTINCT s.company_id
                        FROM user_admin_scopes s
                        WHERE s.user_id = :userId
                        AND s.active = 1
                        AND s.scope_type = 'COMPANY'
                        AND s.company_id IN :companyIds
                        """, nativeQuery = true)
        List<Long> findAllowedCompanyIds(
                        @Param("userId") String userId,
                        @Param("companyIds") Collection<Long> companyIds);
}
