package vn.system.app.modules.sourcelink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.system.app.modules.sourcelink.domain.SourceGroup;

@Repository
public interface SourceGroupRepository extends JpaRepository<SourceGroup, Long> {
}
