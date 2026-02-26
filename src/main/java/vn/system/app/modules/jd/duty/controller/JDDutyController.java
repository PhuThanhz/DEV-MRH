package vn.system.app.modules.jd.duty.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.jd.duty.domain.request.ReqSaveJDDutyDTO;
import vn.system.app.modules.jd.duty.domain.response.ResJDDutyDTO;
import vn.system.app.modules.jd.duty.service.JDDutyService;

@RestController
@RequestMapping("/api/v1/jd-duties")
public class JDDutyController {

    private final JDDutyService service;

    public JDDutyController(JDDutyService service) {
        this.service = service;
    }

    @PostMapping
    @ApiMessage("Lưu mô tả công việc")
    public ResponseEntity<ResJDDutyDTO> save(
            @Valid @RequestBody ReqSaveJDDutyDTO req) {

        return ResponseEntity.ok(service.save(req));
    }

    @GetMapping("/jd/{jdId}")
    @ApiMessage("Lấy mô tả công việc theo JD")
    public ResponseEntity<ResJDDutyDTO> getByJD(
            @PathVariable("jdId") Long jdId) {

        return ResponseEntity.ok(service.fetchByJD(jdId));
    }
}
