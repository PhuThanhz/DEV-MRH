package vn.system.app.modules.jdflow.service;

import org.springframework.stereotype.Service;

import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.jdflow.domain.JobDescriptionFlow;
import vn.system.app.modules.jdflow.domain.JobDescriptionFlow.FlowStatus;
import vn.system.app.modules.jdflow.domain.response.ResJobDescriptionFlowDTO;
import vn.system.app.modules.jdflow.repository.JobDescriptionFlowRepository;

@Service
public class JobDescriptionFlowQueryService {

    private final JobDescriptionFlowRepository flowRepo;

    public JobDescriptionFlowQueryService(JobDescriptionFlowRepository flowRepo) {
        this.flowRepo = flowRepo;
    }

    // =====================================================
    // LẤY FLOW THEO ID – PRIVATE
    // =====================================================
    private JobDescriptionFlow getFlowOrThrow(Long flowId) {
        if (flowId == null) {
            throw new IdInvalidException("flowId không được để trống");
        }

        return flowRepo.findById(flowId)
                .orElseThrow(() -> new IdInvalidException("Flow không tồn tại"));
    }

    // =====================================================
    // LẤY FLOW Ở TRẠNG THÁI PENDING
    // =====================================================
    public JobDescriptionFlow getPendingFlow(Long flowId) {
        JobDescriptionFlow flow = getFlowOrThrow(flowId);

        if (flow.getStatus() != FlowStatus.PENDING) {
            throw new IdInvalidException("Flow không còn ở trạng thái đang duyệt (PENDING)");
        }

        return flow;
    }

    // =====================================================
    // LẤY FLOW Ở TRẠNG THÁI WAITING_ISSUE
    // =====================================================
    public JobDescriptionFlow getWaitingIssueFlow(Long flowId) {
        JobDescriptionFlow flow = getFlowOrThrow(flowId);

        if (flow.getStatus() != FlowStatus.WAITING_ISSUE) {
            throw new IdInvalidException("Flow chưa sẵn sàng để ban hành (WAITING_ISSUE)");
        }

        return flow;
    }

    // =====================================================
    // LẤY FLOW MỚI NHẤT THEO JD
    // =====================================================
    public ResJobDescriptionFlowDTO getByJobDescriptionId(Long jdId) {

        if (jdId == null) {
            throw new IdInvalidException("jobDescriptionId không được để trống");
        }

        JobDescriptionFlow flow = flowRepo
                .findTopByJobDescriptionIdOrderByCreatedAtDesc(jdId)
                .orElseThrow(() -> new IdInvalidException("JD chưa có luồng duyệt"));

        return toRes(flow);
    }

    // =====================================================
    // ENTITY → DTO
    // =====================================================
    private ResJobDescriptionFlowDTO toRes(JobDescriptionFlow flow) {

        ResJobDescriptionFlowDTO dto = new ResJobDescriptionFlowDTO();

        dto.setId(flow.getId());
        dto.setJobDescriptionId(flow.getJobDescriptionId());
        dto.setFromUserId(flow.getFromUserId());
        dto.setToUserId(flow.getToUserId());
        dto.setStatus(flow.getStatus().name());
        dto.setCreatedAt(flow.getCreatedAt());

        return dto;
    }
}