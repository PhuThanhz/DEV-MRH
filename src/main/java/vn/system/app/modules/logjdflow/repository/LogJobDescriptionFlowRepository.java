package vn.system.app.modules.logjdflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.logjdflow.domain.LogJobDescriptionFlow;

@Repository
public interface LogJobDescriptionFlowRepository
        extends JpaRepository<LogJobDescriptionFlow, Long> {

    // Lấy log theo flow, sắp xếp để UI hiển thị đẹp
    List<LogJobDescriptionFlow> findByFlowIdOrderByCreatedAtAsc(Long flowId);

}
