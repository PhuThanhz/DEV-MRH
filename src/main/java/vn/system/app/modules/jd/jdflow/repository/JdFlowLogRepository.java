package vn.system.app.modules.jd.jdflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.jd.jdflow.domain.JdFlowLog;

@Repository
public interface JdFlowLogRepository extends JpaRepository<JdFlowLog, Long> {

    // timeline log (tăng dần)
    List<JdFlowLog> findByJobDescriptionIdOrderByCreatedAtAsc(Long jdId);

    // thêm dòng này để lấy log mới nhất
    List<JdFlowLog> findByJobDescriptionIdOrderByCreatedAtDesc(Long jdId);

}