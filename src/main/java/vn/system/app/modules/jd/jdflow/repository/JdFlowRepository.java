package vn.system.app.modules.jd.jdflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.jd.jdflow.domain.JdFlow;

@Repository
public interface JdFlowRepository extends JpaRepository<JdFlow, Long> {

    JdFlow findByJobDescriptionId(Long jdId);

    List<JdFlow> findByCurrentUserIdAndStatus(Long userId, String status);

    List<JdFlow> findByCurrentUserIdAndStatusIn(Long userId, List<String> statuses);

}