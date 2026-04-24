package vn.system.app.modules.sharetoken.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.sharetoken.domain.ShareTokenAccessLog;

@Repository
public interface ShareTokenAccessLogRepository extends JpaRepository<ShareTokenAccessLog, Long> {

    List<ShareTokenAccessLog> findByShareTokenIdOrderByAccessedAtDesc(Long tokenId);
}