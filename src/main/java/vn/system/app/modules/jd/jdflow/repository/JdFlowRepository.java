package vn.system.app.modules.jd.jdflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.jd.jdflow.domain.JdFlow;

@Repository
public interface JdFlowRepository extends JpaRepository<JdFlow, Long> {

    JdFlow findByJobDescriptionId(Long jdId);

    // Query cũ (có thể giữ hoặc xóa nếu không dùng)
    List<JdFlow> findByCurrentUserIdAndStatus(Long userId, String status);

    /**
     * Dùng cho Inbox - JD chờ xử lý
     * Chỉ lấy các trạng thái: IN_REVIEW, APPROVED, REJECTED
     * (Đã bỏ RETURNED)
     */
    List<JdFlow> findByCurrentUserIdAndStatusIn(Long userId, List<String> statuses);

}