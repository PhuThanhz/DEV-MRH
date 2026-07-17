package vn.system.app.modules.evaluation.domain.response;

public record ResEvaluationTaskCountsDTO(
        long myPending,
        long pendingManager,
        long pendingApproval) {
}
