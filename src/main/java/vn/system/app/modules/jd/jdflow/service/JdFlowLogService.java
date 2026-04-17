package vn.system.app.modules.jd.jdflow.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import vn.system.app.modules.jd.jdflow.domain.JdFlowLog;
import vn.system.app.modules.jd.jdflow.domain.response.ResJdFlowLogDTO;
import vn.system.app.modules.jd.jdflow.repository.JdFlowLogRepository;
import vn.system.app.modules.jd.jobdescription.domain.JobDescription;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.userposition.domain.UserPosition;
import vn.system.app.modules.userposition.repository.UserPositionRepository;

import lombok.Getter;

@Service
public class JdFlowLogService {

        private final JdFlowLogRepository repository;
        private final UserPositionRepository userPositionRepository;

        public JdFlowLogService(
                        JdFlowLogRepository repository,
                        UserPositionRepository userPositionRepository) {
                this.repository = repository;
                this.userPositionRepository = userPositionRepository;
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
                return logs.stream()
                                .filter(log -> ("APPROVE".equals(log.getAction())
                                                || "SUBMIT".equals(log.getAction())
                                                || "SUBMIT_TO_FINAL".equals(log.getAction()))
                                                && log.getToUser() != null
                                                && log.getToUser().getId().equals(rejectUserId))
                                .map(JdFlowLog::getFromUser)
                                .findFirst()
                                .orElse(null);
        }

        /*
         * ==========================================
         * FIND NGƯỜI TỪ CHỐI GẦN NHẤT (DÙNG ĐỂ GỬI LẠI DUYỆT TỰ ĐỘNG)
         * ==========================================
         */
        public User findLastRejector(Long jdId) {
                List<JdFlowLog> logs = repository
                                .findByJobDescriptionIdOrderByCreatedAtDesc(jdId);

                // Debug tạm thời (có thể xóa sau khi test ổn)
                logs.stream()
                                .filter(log -> "REJECT".equals(log.getAction()))
                                .findFirst()
                                .ifPresent(log -> {
                                        System.out.println("[DEBUG] Last REJECT for JD " + jdId
                                                        + " by: " + (log.getFromUser() != null
                                                                        ? log.getFromUser().getName() + " (ID="
                                                                                        + log.getFromUser().getId()
                                                                                        + ")"
                                                                        : "null"));
                                });

                return logs.stream()
                                .filter(log -> "REJECT".equals(log.getAction()) && log.getFromUser() != null)
                                .map(JdFlowLog::getFromUser)
                                .findFirst()
                                .orElse(null);
        }

        /*
         * ==========================================
         * FIND LOG TỪ CHỐI GẦN NHẤT
         * ==========================================
         */
        public JdFlowLog findLatestRejectLog(Long jdId) {
                List<JdFlowLog> logs = repository
                                .findByJobDescriptionIdOrderByCreatedAtDesc(jdId);
                return logs.stream()
                                .filter(log -> "REJECT".equals(log.getAction()))
                                .findFirst()
                                .orElse(null);
        }

        /*
         * ==========================================
         * REJECT INFO — trả đủ tên + chức danh + phòng ban + cấp bậc
         * ==========================================
         */
        @Getter
        public static class RejectInfo {
                private final String comment;
                private final String rejectorName;
                private final String rejectorPosition;
                private final String rejectorDepartment;
                private final String rejectorPositionCode;

                public RejectInfo(String comment, String rejectorName,
                                String rejectorPosition, String rejectorDepartment,
                                String rejectorPositionCode) {
                        this.comment = comment;
                        this.rejectorName = rejectorName;
                        this.rejectorPosition = rejectorPosition;
                        this.rejectorDepartment = rejectorDepartment;
                        this.rejectorPositionCode = rejectorPositionCode;
                }
        }

        public RejectInfo findLatestRejectInfo(Long jdId) {
                JdFlowLog log = findLatestRejectLog(jdId);
                if (log == null || log.getFromUser() == null)
                        return null;

                User rejector = log.getFromUser();
                String position = null;
                String department = null;
                String positionCode = null;

                List<UserPosition> ups = userPositionRepository
                                .findByUser_IdAndActiveTrue(rejector.getId());

                if (ups != null && !ups.isEmpty()) {

                        // Chức danh — ưu tiên DEPARTMENT > SECTION > COMPANY
                        position = ups.stream()
                                        .filter(up -> "DEPARTMENT".equals(up.getSource())
                                                        && up.getDepartmentJobTitle() != null
                                                        && up.getDepartmentJobTitle().getJobTitle() != null)
                                        .map(up -> up.getDepartmentJobTitle().getJobTitle().getNameVi())
                                        .findFirst().orElse(null);

                        if (position == null) {
                                position = ups.stream()
                                                .filter(up -> "SECTION".equals(up.getSource())
                                                                && up.getSectionJobTitle() != null
                                                                && up.getSectionJobTitle().getJobTitle() != null)
                                                .map(up -> up.getSectionJobTitle().getJobTitle().getNameVi())
                                                .findFirst().orElse(null);
                        }

                        if (position == null) {
                                position = ups.stream()
                                                .filter(up -> "COMPANY".equals(up.getSource())
                                                                && up.getCompanyJobTitle() != null
                                                                && up.getCompanyJobTitle().getJobTitle() != null)
                                                .map(up -> up.getCompanyJobTitle().getJobTitle().getNameVi())
                                                .findFirst().orElse(null);
                        }

                        // Phòng ban — ưu tiên DEPARTMENT > SECTION
                        department = ups.stream()
                                        .filter(up -> "DEPARTMENT".equals(up.getSource())
                                                        && up.getDepartmentJobTitle() != null
                                                        && up.getDepartmentJobTitle().getDepartment() != null)
                                        .map(up -> up.getDepartmentJobTitle().getDepartment().getName())
                                        .findFirst().orElse(null);

                        if (department == null) {
                                department = ups.stream()
                                                .filter(up -> "SECTION".equals(up.getSource())
                                                                && up.getSectionJobTitle() != null
                                                                && up.getSectionJobTitle().getSection() != null
                                                                && up.getSectionJobTitle().getSection()
                                                                                .getDepartment() != null)
                                                .map(up -> up.getSectionJobTitle().getSection().getDepartment()
                                                                .getName())
                                                .findFirst().orElse(null);
                        }

                        // Cấp bậc — ưu tiên DEPARTMENT > SECTION > COMPANY
                        positionCode = ups.stream()
                                        .filter(up -> "DEPARTMENT".equals(up.getSource())
                                                        && up.getDepartmentJobTitle() != null
                                                        && up.getDepartmentJobTitle().getJobTitle() != null
                                                        && up.getDepartmentJobTitle().getJobTitle()
                                                                        .getPositionLevel() != null)
                                        .map(up -> up.getDepartmentJobTitle().getJobTitle().getPositionLevel()
                                                        .getCode())
                                        .findFirst().orElse(null);

                        if (positionCode == null) {
                                positionCode = ups.stream()
                                                .filter(up -> "SECTION".equals(up.getSource())
                                                                && up.getSectionJobTitle() != null
                                                                && up.getSectionJobTitle().getJobTitle() != null
                                                                && up.getSectionJobTitle().getJobTitle()
                                                                                .getPositionLevel() != null)
                                                .map(up -> up.getSectionJobTitle().getJobTitle().getPositionLevel()
                                                                .getCode())
                                                .findFirst().orElse(null);
                        }

                        if (positionCode == null) {
                                positionCode = ups.stream()
                                                .filter(up -> "COMPANY".equals(up.getSource())
                                                                && up.getCompanyJobTitle() != null
                                                                && up.getCompanyJobTitle().getJobTitle() != null
                                                                && up.getCompanyJobTitle().getJobTitle()
                                                                                .getPositionLevel() != null)
                                                .map(up -> up.getCompanyJobTitle().getJobTitle().getPositionLevel()
                                                                .getCode())
                                                .findFirst().orElse(null);
                        }
                }

                return new RejectInfo(log.getComment(), rejector.getName(),
                                position, department, positionCode);
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