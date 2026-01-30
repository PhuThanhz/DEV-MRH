package vn.system.app.modules.logjdflow.service;

import java.util.List;

import org.springframework.stereotype.Service;

import vn.system.app.modules.logjdflow.domain.LogJobDescriptionFlow;
import vn.system.app.modules.logjdflow.repository.LogJobDescriptionFlowRepository;

@Service
public class LogJobDescriptionFlowService {

    private final LogJobDescriptionFlowRepository repo;

    public LogJobDescriptionFlowService(LogJobDescriptionFlowRepository repo) {
        this.repo = repo;
    }

    /* ===================== GHI LOG ===================== */

    public void log(
            Long jobDescriptionId,
            Long flowId,
            String action,
            Long fromUserId,
            Long toUserId,
            String comment) {

        LogJobDescriptionFlow log = new LogJobDescriptionFlow();
        log.setJobDescriptionId(jobDescriptionId);
        log.setFlowId(flowId);
        log.setAction(action);
        log.setFromUserId(fromUserId);
        log.setToUserId(toUserId);
        log.setComment(comment);

        repo.save(log);
    }

    /* ===================== LẤY LOG ===================== */

    public List<LogJobDescriptionFlow> getByFlowId(Long flowId) {
        return repo.findByFlowIdOrderByCreatedAtAsc(flowId);
    }
}
