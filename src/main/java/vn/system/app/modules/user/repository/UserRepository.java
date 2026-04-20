package vn.system.app.modules.user.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.user.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

    // =========================
    // BASIC QUERY
    // =========================

    User findByEmail(String email);

    boolean existsByEmail(String email);

    User findByRefreshTokenAndEmail(String token, String email);

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

    /**
     * Load 1 user kèm role — dùng trong convertShareLogToDTO
     * Tránh LazyInitializationException khi gọi u.getRole().getName()
     */
    @EntityGraph(attributePaths = { "role" })
    Optional<User> findWithRoleById(String id);
}