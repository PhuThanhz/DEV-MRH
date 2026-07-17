package vn.system.app.modules.jd.jdflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.jd.jdflow.domain.JdFlow;

@Repository
public interface JdFlowRepository extends JpaRepository<JdFlow, Long> {

    JdFlow findByJobDescriptionId(Long jdId);

    // Query cũ (có thể giữ hoặc xóa nếu không dùng)
    List<JdFlow> findByCurrentUserIdAndStatus(String userId, String status);

    /**
     * Dùng cho Inbox - JD chờ xử lý
     * Chỉ lấy các trạng thái: IN_REVIEW, APPROVED, REJECTED
     * (Đã bỏ RETURNED)
     */
    @EntityGraph(attributePaths = {
            "currentUser", "jobDescription", "jobDescription.company", "jobDescription.department",
            "jobDescription.companyJobTitle", "jobDescription.companyJobTitle.jobTitle",
            "jobDescription.departmentJobTitle", "jobDescription.departmentJobTitle.jobTitle",
            "jobDescription.sectionJobTitle", "jobDescription.sectionJobTitle.jobTitle"
    })
    List<JdFlow> findByCurrentUserIdAndStatusIn(String userId, List<String> statuses);

}
