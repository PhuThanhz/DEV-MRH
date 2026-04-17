package vn.system.app.modules.jd.jdflow.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.jd.jdflow.domain.request.ReqApproveJdFlow;
import vn.system.app.modules.jd.jdflow.domain.request.ReqRejectJdFlow;
import vn.system.app.modules.jd.jdflow.domain.request.ReqSubmitJdFlow;
import vn.system.app.modules.jd.jdflow.domain.request.ReqIssueJdFlow;
import vn.system.app.modules.jd.jdflow.domain.response.ResJdApproverDTO;
import vn.system.app.modules.jd.jdflow.domain.response.ResJdFlowDTO;
import vn.system.app.modules.jd.jdflow.domain.response.ResJdFlowLogDTO;
import vn.system.app.modules.jd.jdflow.domain.response.ResJdInboxDTO;
import vn.system.app.modules.jd.jdflow.service.JdFlowService;
import vn.system.app.modules.jd.jdflow.service.JdFlowLogService;

@RestController
@RequestMapping("/api/v1")
public class JdFlowController {
        private final JdFlowService jdFlowService;
        private final JdFlowLogService jdFlowLogService;

        public JdFlowController(
                        JdFlowService jdFlowService,
                        JdFlowLogService jdFlowLogService) {

                this.jdFlowService = jdFlowService;
                this.jdFlowLogService = jdFlowLogService;
        }

        /*
         * ==========================================
         * GET FLOW BY JD
         * ==========================================
         */
        @GetMapping("/jd-flow/{jdId}")
        @ApiMessage("Fetch JD Flow")
        public ResponseEntity<ResJdFlowDTO> getFlow(@PathVariable Long jdId) {
                return ResponseEntity.ok(
                                jdFlowService.fetchFlowByJd(jdId));
        }

        /*
         * ==========================================
         * JD ĐANG CHỜ TÔI DUYỆT
         * ==========================================
         */
        @GetMapping("/jd-flow/inbox")
        @ApiMessage("JD đang chờ tôi duyệt")
        public ResponseEntity<List<ResJdInboxDTO>> fetchInbox() {
                return ResponseEntity.ok(
                                jdFlowService.fetchInbox());
        }

        /*
         * ==========================================
         * FETCH JD APPROVERS
         * ==========================================
         */
        @GetMapping("/jd-flow/approvers")
        @ApiMessage("Fetch JD Approvers")
        public ResponseEntity<List<ResJdApproverDTO>> fetchApprovers() {
                return ResponseEntity.ok(
                                jdFlowService.fetchApprovers());
        }

        /*
         * ==========================================
         * TIMELINE DUYỆT JD
         * ==========================================
         */
        @GetMapping("/jd-flow/logs/{jdId}")
        @ApiMessage("Timeline duyệt JD")
        public ResponseEntity<List<ResJdFlowLogDTO>> fetchLogs(
                        @PathVariable Long jdId) {
                return ResponseEntity.ok(
                                jdFlowLogService.fetchLogs(jdId));
        }

        /*
         * ==========================================
         * SUBMIT JD
         * ==========================================
         */
        @PostMapping("/jd-flow/submit")
        @ApiMessage("Gửi JD duyệt thành công")
        public ResponseEntity<ResJdFlowDTO> submitFlow(
                        @Valid @RequestBody ReqSubmitJdFlow req) {

                jdFlowService.submitFlow(
                                req.getJdId(),
                                req.getNextUserId());

                return ResponseEntity.ok(
                                jdFlowService.fetchFlowByJd(req.getJdId()));
        }

        /*
         * ==========================================
         * APPROVE JD
         * ==========================================
         */
        @PostMapping("/jd-flow/approve")
        @ApiMessage("Duyệt JD thành công")
        public ResponseEntity<ResJdFlowDTO> approveFlow(
                        @Valid @RequestBody ReqApproveJdFlow req) {

                jdFlowService.approveFlow(
                                req.getJdId(),
                                req.getNextUserId());

                return ResponseEntity.ok(
                                jdFlowService.fetchFlowByJd(req.getJdId()));
        }

        /*
         * ==========================================
         * REJECT JD
         * ==========================================
         */
        @PostMapping("/jd-flow/reject")
        @ApiMessage("Từ chối JD thành công")
        public ResponseEntity<ResJdFlowDTO> rejectFlow(
                        @Valid @RequestBody ReqRejectJdFlow req) {

                jdFlowService.rejectFlow(
                                req.getJdId(),
                                req.getComment());

                return ResponseEntity.ok(
                                jdFlowService.fetchFlowByJd(req.getJdId()));
        }

        /*
         * ==========================================
         * RECALL JD — Thu hồi JD đang chờ duyệt
         * ==========================================
         */
        @PostMapping("/jd-flow/recall/{jdId}")
        @ApiMessage("Thu hồi JD thành công")
        public ResponseEntity<ResJdFlowDTO> recallFlow(@PathVariable Long jdId) {

                jdFlowService.recallFlow(jdId);

                return ResponseEntity.ok(
                                jdFlowService.fetchFlowByJd(jdId));
        }

        /*
         * ==========================================
         * ISSUE JD
         * ==========================================
         */
        @PostMapping("/jd-flow/issue")
        @ApiMessage("Ban hành JD thành công")
        public ResponseEntity<ResJdFlowDTO> issueFlow(
                        @Valid @RequestBody ReqIssueJdFlow req) {

                jdFlowService.issueFlow(req.getJdId());

                return ResponseEntity.ok(
                                jdFlowService.fetchFlowByJd(req.getJdId()));
        }

        /*
         * ==========================================
         * FETCH JD ISSUERS
         * ==========================================
         */
        @GetMapping("/jd-flow/issuers")
        @ApiMessage("Fetch JD Issuers")
        public ResponseEntity<List<ResJdApproverDTO>> fetchIssuers() {
                return ResponseEntity.ok(
                                jdFlowService.fetchIssuers());
        }
}