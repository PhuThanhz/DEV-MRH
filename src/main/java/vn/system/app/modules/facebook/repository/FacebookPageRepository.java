package vn.system.app.modules.facebook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.system.app.modules.facebook.domain.FacebookPage;

public interface FacebookPageRepository extends JpaRepository<FacebookPage, Long> {
    boolean existsByPageId(String pageId);
}
