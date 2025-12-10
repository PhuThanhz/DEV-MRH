package vn.system.app.modules.sourcelink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.system.app.modules.sourcelink.domain.SourceGroup;

import java.util.Optional;

public interface SourceGroupRepository
        extends JpaRepository<SourceGroup, Long>, JpaSpecificationExecutor<SourceGroup> {

    // ============================================================
    // TÌM NHÓM THEO TÊN
    // ============================================================
    Optional<SourceGroup> findByName(String name);
}
