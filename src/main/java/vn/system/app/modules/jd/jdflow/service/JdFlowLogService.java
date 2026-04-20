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
         * FIND NGƯỜI GỬI GẦN NHẤT (DÙNG CHO INBOX & REJECT)
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

        /**
         * ==========================================
         * METHOD MỚI: TÌM NGƯỜI GỬI TRƯỚC ĐÓ TRƯỚC KHI BỊ TỪ CHỐI
         * Dùng khi User2 bị User3 từ chối → muốn gửi lại cho User1
         * 
         * Logic: Tìm người đã gửi cho User2 (người trước User2 trong chuỗi)
         * ==========================================
         */
        public User findLastSenderBeforeReject(Long jdId) {
                List<JdFlowLog> logs = repository
                                .findByJobDescriptionIdOrderByCreatedAtAsc(jdId);

                // 1. Tìm vị trí REJECT gần nhất
                int rejectIndex = -1;
                for (int i = logs.size() - 1; i >= 0; i--) {
                        if ("REJECT".equals(logs.get(i).getAction())) {
                                rejectIndex = i;
                                break;
                        }
                }

                if (rejectIndex == -1)
                        return null;

                JdFlowLog rejectLog = logs.get(rejectIndex);

                if (rejectLog.getToUser() == null)
                        return null;

                String receiverId = rejectLog.getToUser().getId(); // User2

                // 2. Duyệt NGƯỢC để tìm người gửi gần nhất cho User2
                for (int i = rejectIndex - 1; i >= 0; i--) {
                        JdFlowLog log = logs.get(i);

                        if (log.getToUser() != null
                                        && log.getToUser().getId().equals(receiverId)
                                        && ("SUBMIT".equals(log.getAction())
                                                        || "APPROVE".equals(log.getAction())
                                                        || "SUBMIT_TO_FINAL".equals(log.getAction()))) {

                                return log.getFromUser(); // ✅ User1
                        }
                }

                return null;
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
         * TÌM LOG GỬI VỀ GẦN NHẤT (có comment [TRẢ VỀ])
         * ==========================================
         */
        public JdFlowLog findLatestReturnLog(Long jdId) {
                List<JdFlowLog> logs = repository
                                .findByJobDescriptionIdOrderByCreatedAtDesc(jdId);
                return logs.stream()
                                .filter(log -> ("SUBMIT".equals(log.getAction())
                                                || "SUBMIT_TO_FINAL".equals(log.getAction()))
                                                && log.getComment() != null
                                                && log.getComment().startsWith("[TRẢ VỀ]"))
                                .findFirst()
                                .orElse(null);
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