package vn.system.app.modules.jd.requirement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.jd.requirement.domain.request.ReqSaveJDRequirementDTO;
import vn.system.app.modules.jd.requirement.domain.response.ResJDRequirementDTO;
import vn.system.app.modules.jd.requirement.service.JDRequirementService;

@RestController
@RequestMapping("/api/v1/jd-requirements")
public class JDRequirementController {

    private final JDRequirementService service;

    public JDRequirementController(JDRequirementService service) {
        this.service = service;
    }

    @PostMapping
    @ApiMessage("Lưu yêu cầu vị trí")
    public ResponseEntity<ResJDRequirementDTO> save(
            @Valid @RequestBody ReqSaveJDRequirementDTO req) {

        return ResponseEntity.ok(service.save(req));
    }

    @GetMapping("/jd/{jdId}")
    @ApiMessage("Lấy yêu cầu vị trí theo JD")
    public ResponseEntity<ResJDRequirementDTO> getByJD(
            @PathVariable("jdId") Long jdId) {

        return ResponseEntity.ok(service.fetchByJD(jdId));
    }
}