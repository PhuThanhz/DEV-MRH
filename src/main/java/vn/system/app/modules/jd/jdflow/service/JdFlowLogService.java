package vn.system.app.modules.jd.jdflow.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import vn.system.app.modules.jd.jdflow.domain.JdFlowLog;
import vn.system.app.modules.jd.jdflow.domain.response.ResJdFlowLogDTO;
import vn.system.app.modules.jd.jdflow.repository.JdFlowLogRepository;
import vn.system.app.modules.jd.jobdescription.domain.JobDescription;
import vn.system.app.modules.user.domain.User;

@Service
public class JdFlowLogService {

    private final JdFlowLogRepository repository;

    public JdFlowLogService(JdFlowLogRepository repository) {
        this.repository = repository;
    }

    /*
     * ==========================================
     * SAVE LOG
     * ==========================================
     */
    public void saveLog(JobDescription jd, User from, User to, String action, String comment) {

        JdFlowLog log = new JdFlowLog();

        log.setJobDescription(jd);
        log.setFromUser(from);
        log.setToUser(to);
        log.setAction(action);
        log.setComment(comment);

        repository.save(log);
    }

    /*
     * ==========================================
     * FIND USER ĐÃ SUBMIT JD (NGƯỜI TẠO LUỒNG)
     * ==========================================
     */
    public User findSubmitUser(Long jdId) {

        List<JdFlowLog> logs = repository
                .findByJobDescriptionIdOrderByCreatedAtAsc(jdId);

        return logs.stream()
                .filter(log -> "SUBMIT".equals(log.getAction()))
                .map(JdFlowLog::getFromUser)
                .findFirst()
                .orElse(null);
    }

    /*
     * ==========================================
     * FIND NGƯỜI GỬI GẦN NHẤT (DÙNG CHO INBOX)
     * ==========================================
     */
    public User findLastSender(Long jdId) {

        List<JdFlowLog> logs = repository
                .findByJobDescriptionIdOrderByCreatedAtDesc(jdId);

        return logs.stream()
                .map(JdFlowLog::getFromUser)
                .filter(user -> user != null)
                .findFirst()
                .orElse(null);
    }

    /*
     * ==========================================
     * FIND PREVIOUS APPROVER (DÙNG CHO REJECT)
     * ==========================================
     */
    public User findPreviousApprover(Long jdId, Long rejectUserId) {
        List<JdFlowLog> logs = repository.findByJobDescriptionIdOrderByCreatedAtDesc(jdId);
        for (JdFlowLog log : logs) {
            if ("APPROVE".equals(log.getAction()) || "SUBMIT".equals(log.getAction())) {
                if (!log.getFromUser().getId().equals(rejectUserId)) {
                    return log.getFromUser();
                }
            }
        }
        return null;
    }

    /*
     * ==========================================
     * FETCH LOG TIMELINE
     * ==========================================
     */
    public List<ResJdFlowLogDTO> fetchLogs(Long jdId) {

        List<JdFlowLog> logs = repository
                .findByJobDescriptionIdOrderByCreatedAtAsc(jdId);

        return logs.stream().map(log -> {

            ResJdFlowLogDTO dto = new ResJdFlowLogDTO();

            dto.setId(log.getId());
            dto.setJdId(log.getJobDescription().getId());
            dto.setAction(log.getAction());
            dto.setComment(log.getComment());
            dto.setCreatedAt(log.getCreatedAt());

            if (log.getFromUser() != null) {
                dto.setFromUser(new ResJdFlowLogDTO.UserInfo(
                        log.getFromUser().getId(),
                        log.getFromUser().getName()));
            }

            if (log.getToUser() != null) {
                dto.setToUser(new ResJdFlowLogDTO.UserInfo(
                        log.getToUser().getId(),
                        log.getToUser().getName()));
            }

            return dto;

        }).collect(Collectors.toList());
    }

}