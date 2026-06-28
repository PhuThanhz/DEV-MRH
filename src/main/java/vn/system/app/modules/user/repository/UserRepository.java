package vn.system.app.modules.user.repository;

import java.util.Optional;
import java.util.List;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.user.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

    // =========================
    // BASIC QUERY
    // =========================

    User findByEmail(String email);

    boolean existsByEmail(String email);

    long countByIdIn(List<String> ids);

    // =========================
    // FETCH JOIN USING ENTITY GRAPH
    // =========================

    /**
     * Load user kèm userInfo + role trong 1 query duy nhất
     */
    @Override
    @EntityGraph(attributePaths = { "userInfo", "role" })
    Page<User> findAll(Specification<User> spec, Pageable pageable);

    /**
     * Load 1 user kèm userInfo + role
     */
    @EntityGraph(attributePaths = { "userInfo", "role" })
    User findWithUserInfoById(String id);

    @EntityGraph(attributePaths = { "userInfo", "role", "role.permissions" })
    User findWithAuthByEmail(String email);

    /**
     * Load 1 user kèm role — dùng trong convertShareLogToDTO
     * Tránh LazyInitializationException khi gọi u.getRole().getName()
     */
    @EntityGraph(attributePaths = { "role" })
    Optional<User> findWithRoleById(String id);

    // =========================
    // ATOMIC UPDATE
    // =========================

    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :now, u.lastLoginIp = :ip WHERE u.id = :id AND (u.lastLoginAt IS NULL OR u.lastLoginAt < :threshold)")
    int updateLastLogin(@Param("id") String id, @Param("ip") String ip, @Param("now") Instant now,
            @Param("threshold") Instant threshold);

    /**
     * Tối ưu hóa: Tìm user có permission cụ thể và thuộc công ty cụ thể
     */
    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN u.role r " +
            "JOIN r.permissions p " +
            "JOIN UserPosition up ON up.user.id = u.id " +
            "LEFT JOIN up.companyJobTitle cjt " +
            "LEFT JOIN cjt.company cjt_company " +
            "LEFT JOIN up.departmentJobTitle djt " +
            "LEFT JOIN djt.department djt_department " +
            "LEFT JOIN djt_department.company djt_company " +
            "LEFT JOIN up.sectionJobTitle sjt " +
            "LEFT JOIN sjt.section sjt_section " +
            "LEFT JOIN sjt_section.department sjt_department " +
            "LEFT JOIN sjt_department.company sjt_company " +
            "WHERE p.name IN :permissionNames " +
            "AND up.active = true " +
            "AND (:checkCompany = false OR " +
            "     cjt_company.id IN :companyIds OR " +
            "     djt_company.id IN :companyIds OR " +
            "     sjt_company.id IN :companyIds)")
    List<User> findUsersByPermissionAndCompany(
            @Param("permissionNames") List<String> permissionNames,
            @Param("companyIds") List<Long> companyIds,
            @Param("checkCompany") boolean checkCompany);

    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN u.role r " +
            "JOIN r.permissions p " +
            "WHERE p.name IN :permissionNames")
    List<User> findUsersByPermissionNames(@Param("permissionNames") List<String> permissionNames);

    @Query("SELECT DISTINCT u.id FROM User u " +
            "JOIN UserPosition up ON up.user.id = u.id " +
            "LEFT JOIN up.companyJobTitle cjt " +
            "LEFT JOIN cjt.company cjt_company " +
            "LEFT JOIN up.departmentJobTitle djt " +
            "LEFT JOIN djt.department djt_department " +
            "LEFT JOIN djt_department.company djt_company " +
            "LEFT JOIN up.sectionJobTitle sjt " +
            "LEFT JOIN sjt.section sjt_section " +
            "LEFT JOIN sjt_section.department sjt_department " +
            "LEFT JOIN sjt_department.company sjt_company " +
            "WHERE up.active = true " +
            "AND (cjt_company.id = :companyId OR " +
            "     djt_company.id = :companyId OR " +
            "     sjt_company.id = :companyId)")
    List<String> findActiveUserIdsByCompany(@Param("companyId") Long companyId);
}
