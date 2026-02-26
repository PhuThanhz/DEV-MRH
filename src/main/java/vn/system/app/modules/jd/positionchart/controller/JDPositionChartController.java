package vn.system.app.modules.jd.positionchart.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.jd.positionchart.domain.request.ReqCreateJDPositionChartDTO;
import vn.system.app.modules.jd.positionchart.domain.response.*;
import vn.system.app.modules.jd.positionchart.service.JDPositionChartService;

@RestController
@RequestMapping("/api/v1/jd-position-charts")
public class JDPositionChartController {

    private final JDPositionChartService service;

    public JDPositionChartController(JDPositionChartService service) {
        this.service = service;
    }

    @PostMapping
    @ApiMessage("Tạo node sơ đồ vị trí công việc")
    public ResponseEntity<ResJDPositionChartNodeDTO> create(
            @Valid @RequestBody ReqCreateJDPositionChartDTO req) {

        return ResponseEntity.ok(service.create(req));
    }

    @GetMapping("/jd/{jdId}")
    @ApiMessage("Lấy sơ đồ vị trí theo JD")
    public ResponseEntity<ResJDPositionChartGraphDTO> getGraph(
            @PathVariable("jdId") Long jdId) {

        return ResponseEntity.ok(service.getGraph(jdId));
    }
}