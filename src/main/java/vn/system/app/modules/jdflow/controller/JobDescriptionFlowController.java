package vn.system.app.modules.jdflow.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.jdflow.domain.request.ReqApproveFlow;
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

        // =====================================================
        // 1. GỬI JD ĐI DUYỆT LẦN ĐẦU
        // Quyền: JD_SUBMIT (check trong service)
        // =====================================================
        @PostMapping
        @ApiMessage("Gửi JD đi duyệt")
        public ResponseEntity<ResJobDescriptionFlowDTO> createFlow(
                        @RequestBody ReqCreateFlow req) throws IdInvalidException {

                ResJobDescriptionFlowDTO result = flowService.createFlow(req);
                return ResponseEntity.status(HttpStatus.CREATED).body(result);
        }

        // =====================================================
        // 2. DUYỆT & CHUYỂN CẤP
        // Quyền: JD_APPROVE
        // =====================================================
        @PostMapping("/{flowId}/approve")
        @ApiMessage("Duyệt JD và chuyển cấp")
        public ResponseEntity<ResJobDescriptionFlowDTO> approve(
                        @PathVariable Long flowId,
                        @RequestBody ReqApproveFlow req) throws IdInvalidException {

                ResJobDescriptionFlowDTO result = flowService.approve(flowId, req.getNextUserId());
                return ResponseEntity.ok(result);
        }

        // =====================================================
        // 3. TỪ CHỐI DUYỆT
        // Quyền: JD_APPROVE
        // =====================================================
        @PostMapping("/{flowId}/reject")
        @ApiMessage("Từ chối JD")
        public ResponseEntity<ResJobDescriptionFlowDTO> reject(
                        @PathVariable Long flowId,
                        @RequestBody ReqRejectFlow req) throws IdInvalidException {

                ResJobDescriptionFlowDTO result = flowService.reject(flowId, req);
                return ResponseEntity.ok(result);
        }

        // =====================================================
        // 4. BAN HÀNH JD
        // Quyền: JD_ISSUE
        // =====================================================
        @PostMapping("/{flowId}/issue")
        @ApiMessage("Ban hành JD")
        public ResponseEntity<ResJobDescriptionFlowDTO> issue(
                        @PathVariable Long flowId) throws IdInvalidException {

                ResJobDescriptionFlowDTO result = flowService.issue(flowId);
                return ResponseEntity.ok(result);
        }

        // =====================================================
        // 5. GET FLOW MỚI NHẤT CỦA MỘT JD
        // =====================================================
        @GetMapping("/by-jd/{jdId}")
        @ApiMessage("Xem luồng duyệt hiện tại của JD")
        public ResponseEntity<ResJobDescriptionFlowDTO> getByJobDescription(
                        @PathVariable Long jdId) throws IdInvalidException {

                ResJobDescriptionFlowDTO result = queryService.getByJobDescriptionId(jdId);
                return ResponseEntity.ok(result);
        }

        // =====================================================
        // 6. LẤY DANH SÁCH NGƯỜI DUYỆT CAO CẤP HƠN USER HIỆN TẠI
        // =====================================================
        @GetMapping("/approvers/current")
        @ApiMessage("Danh sách người có thể duyệt (cấp cao hơn bạn)")
        public ResponseEntity<List<ResApproverDTO>> getApproversHigher()
                        throws IdInvalidException {

                List<ResApproverDTO> list = flowService.getApproversHigherThanCurrentUser();
                return ResponseEntity.ok(list);
        }
}