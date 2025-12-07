package vn.system.app.modules.sourcelink.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.system.app.modules.sourcelink.domain.SourceGroup;
import vn.system.app.modules.sourcelink.domain.SourceLink;
import vn.system.app.modules.sourcelink.domain.SourceLink.ProcessingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceLinkRepository extends JpaRepository<SourceLink, Long> {
    Page<SourceLink> findAllByGroup(SourceGroup group, Pageable pageable);

    Page<SourceLink> findAllByGroupAndStatus(SourceGroup group, ProcessingStatus status, Pageable pageable);
}
