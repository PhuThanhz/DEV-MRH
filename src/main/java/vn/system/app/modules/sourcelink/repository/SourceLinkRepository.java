package vn.system.app.modules.sourcelink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.system.app.modules.sourcelink.domain.SourceLink;
import java.util.List;

@Repository
public interface SourceLinkRepository extends JpaRepository<SourceLink, Long> {
    List<SourceLink> findByStatus(SourceLink.ProcessingStatus status);

    // Nếu bạn cần lấy link theo group
    List<SourceLink> findByGroupId(Long groupId);
}
