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
        // Kiểm tra JD có flow đang ở trạng thái cụ thể hay không
        // Dùng ENUM → tránh sai chính tả
        // ============================================================
        boolean existsByJobDescriptionIdAndStatus(
                        Long jobDescriptionId,
                        FlowStatus status);

        // ============================================================
        // Lấy flow theo JD + Status (ví dụ WAITING_ISSUE để CEO ban hành)
        // ============================================================
        Optional<JobDescriptionFlow> findByJobDescriptionIdAndStatus(
                        Long jobDescriptionId,
                        FlowStatus status);

        // ============================================================
        // ⭐ HÀM QUAN TRỌNG NHẤT ⭐
        // Lấy FLOW MỚI NHẤT của JD — luôn là flow người dùng cần thao tác
        // ============================================================
        Optional<JobDescriptionFlow> findTopByJobDescriptionIdOrderByCreatedAtDesc(
                        Long jobDescriptionId);
}