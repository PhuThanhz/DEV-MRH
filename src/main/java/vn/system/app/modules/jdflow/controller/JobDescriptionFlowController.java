package vn.system.app.modules.jdflow.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.jdflow.domain.request.ReqCreateFlow;
import vn.system.app.modules.jdflow.domain.request.ReqRejectFlow;
import vn.system.app.modules.jdflow.domain.response.ResApproverDTO;
import vn.system.app.modules.jdflow.domain.response.ResJobDescriptionFlowDTO;
import vn.system.app.modules.jdflow.service.JobDescriptionFlowQueryService;
import vn.system.app.modules.jdflow.service.JobDescriptionFlowService;

@RestController
@RequestMapping("/api/v1/jd-flows")
public class JobDescriptionFlowController {

    private final JobDescriptionFlowService flowService;
    private final JobDescriptionFlowQueryService queryService;

    public JobDescriptionFlowController(
            JobDescriptionFlowService flowService,
            JobDescriptionFlowQueryService queryService) {

        this.flowService = flowService;
        this.queryService = queryService;
    }

    /*
     * =====================================================
     * 1. GỬI JD ĐI DUYỆT LẦN ĐẦU
     * =====================================================
     */
    @PostMapping
    @ApiMessage("Gửi JD đi duyệt")
    public ResponseEntity<ResJobDescriptionFlowDTO> createFlow(
            @RequestBody ReqCreateFlow req)
            throws IdInvalidException {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(flowService.createFlow(req));
    }

    /*
     * =====================================================
     * 2. DUYỆT & CHUYỂN CẤP
     * =====================================================
     */
    public static class ReqApproveFlow {
        public Long nextUserId;
    }

    @PostMapping("/{flowId}/approve")
    @ApiMessage("Duyệt JD và chuyển cấp")
    public ResponseEntity<ResJobDescriptionFlowDTO> approve(
            @PathVariable Long flowId,
            @RequestBody ReqApproveFlow req)
            throws IdInvalidException {

        return ResponseEntity.status(HttpStatus.OK)
                .body(flowService.approve(flowId, req.nextUserId));
    }

    /*
     * =====================================================
     * 3. TỪ CHỐI
     * =====================================================
     */
    @PostMapping("/{flowId}/reject")
    @ApiMessage("Từ chối JD")
    public ResponseEntity<ResJobDescriptionFlowDTO> reject(
            @PathVariable Long flowId,
            @RequestBody ReqRejectFlow req)
            throws IdInvalidException {

        return ResponseEntity.status(HttpStatus.OK)
                .body(flowService.reject(flowId, req));
    }

    /*
     * =====================================================
     * 4. BAN HÀNH JD
     * =====================================================
     */
    @PostMapping("/{flowId}/issue")
    @ApiMessage("Ban hành JD")
    public ResponseEntity<ResJobDescriptionFlowDTO> issue(
            @PathVariable Long flowId)
            throws IdInvalidException {

        return ResponseEntity.status(HttpStatus.OK)
                .body(flowService.issue(flowId));
    }

    /*
     * =====================================================
     * 5. XEM FLOW MỚI NHẤT CỦA MỘT JD
     * =====================================================
     */
    @GetMapping("/by-jd/{jdId}")
    @ApiMessage("Xem luồng duyệt theo JD")
    public ResponseEntity<ResJobDescriptionFlowDTO> getByJobDescription(
            @PathVariable Long jdId)
            throws IdInvalidException {

        return ResponseEntity.status(HttpStatus.OK)
                .body(queryService.getByJobDescriptionId(jdId));
    }

    /*
     * =====================================================
     * 6. LẤY DANH SÁCH NGƯỜI DUYỆT CAO CẤP HƠN USER HIỆN TẠI
     * =====================================================
     */
    @GetMapping("/approvers/current")
    @ApiMessage("Danh sách người có thể duyệt (cao cấp hơn bạn)")
    public ResponseEntity<List<ResApproverDTO>> getApproversHigher()
            throws IdInvalidException {

        return ResponseEntity.status(HttpStatus.OK)
                .body(flowService.getApproversHigherThanCurrentUser());
    }
}
