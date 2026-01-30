package vn.system.app.modules.logjdflow.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.logjdflow.domain.LogJobDescriptionFlow;
import vn.system.app.modules.logjdflow.domain.response.ResLogJobDescriptionFlowDTO;
import vn.system.app.modules.logjdflow.service.LogJobDescriptionFlowService;

@RestController
@RequestMapping("/api/v1/log-jd-flows")
public class LogJobDescriptionFlowController {

    private final LogJobDescriptionFlowService logService;

    public LogJobDescriptionFlowController(LogJobDescriptionFlowService logService) {
        this.logService = logService;
    }

    /*
     * =====================================================
     * XEM LỊCH SỬ DUYỆT JD THEO FLOW
     * =====================================================
     */
    @GetMapping("/by-flow/{flowId}")
    @ApiMessage("Xem lịch sử duyệt JD")
    public ResponseEntity<List<ResLogJobDescriptionFlowDTO>> getLogsByFlow(
            @PathVariable Long flowId) {

        List<LogJobDescriptionFlow> logs = logService.getByFlowId(flowId);

        List<ResLogJobDescriptionFlowDTO> res = logs.stream()
                .map(l -> {
                    ResLogJobDescriptionFlowDTO dto = new ResLogJobDescriptionFlowDTO();
                    dto.setId(l.getId());
                    dto.setAction(l.getAction());
                    dto.setFromUserId(l.getFromUserId());
                    dto.setToUserId(l.getToUserId());
                    dto.setComment(l.getComment());
                    dto.setCreatedAt(l.getCreatedAt());
                    dto.setCreatedBy(l.getCreatedBy());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(res);
    }
}
