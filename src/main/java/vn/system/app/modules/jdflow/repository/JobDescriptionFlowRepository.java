package vn.system.app.modules.jdflow.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.jdflow.domain.JobDescriptionFlow;
import vn.system.app.modules.jdflow.domain.JobDescriptionFlow.FlowStatus;

@Repository
public interface JobDescriptionFlowRepository
        extends JpaRepository<JobDescriptionFlow, Long> {

    // ============================================================
    // ⚠️ Không khuyến khích dùng — vì một JD có thể có nhiều Flow
    // Chỉ dùng nếu BUSINESS đảm bảo mỗi JD chỉ tạo đúng 1 Flow.
    // Luôn ưu tiên: findTopByJobDescriptionIdOrderByCreatedAtDesc(...)
    // ============================================================
    Optional<JobDescriptionFlow> findByJobDescriptionId(Long jobDescriptionId);

    // ============================================================
    // Kiểm tra JD có flow đang ở trạng thái cụ thể hay không
    // Dùng ENUM để tránh sai chính tả (ví dụ "pending" ≠ "PENDING")
    // ============================================================
    boolean existsByJobDescriptionIdAndStatus(
            Long jobDescriptionId,
            FlowStatus status);

    // ============================================================
    // Lấy flow theo JD + Status
    // Ví dụ: lấy WAITING_ISSUE để chuẩn bị ban hành
    // ============================================================
    Optional<JobDescriptionFlow> findByJobDescriptionIdAndStatus(
            Long jobDescriptionId,
            FlowStatus status);

    // ============================================================
    // ⭐ HÀM QUAN TRỌNG NHẤT:
    // Lấy flow mới nhất theo JD (để hiển thị trạng thái hiện hành cho UI)
    // Flow mới nhất luôn là flow người dùng cần thao tác
    // ============================================================
    Optional<JobDescriptionFlow> findTopByJobDescriptionIdOrderByCreatedAtDesc(
            Long jobDescriptionId);
}
