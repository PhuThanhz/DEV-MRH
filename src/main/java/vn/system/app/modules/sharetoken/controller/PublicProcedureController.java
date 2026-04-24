package vn.system.app.modules.sharetoken.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import vn.system.app.common.util.annotation.ApiMessage;

import vn.system.app.modules.sharetoken.domain.request.VerifyPinRequest;
import vn.system.app.modules.sharetoken.service.PublicProcedureViewService;

@RestController
@RequestMapping("/public/view")
@RequiredArgsConstructor
public class PublicProcedureController {

    private final PublicProcedureViewService publicViewService;

    // =====================================================
    // XEM QUY TRÌNH QUA LINK PUBLIC (không cần JWT)
    // =====================================================
    @GetMapping("/{token}")
    @ApiMessage("Xem quy trình qua link chia sẻ")
    public ResponseEntity<?> viewByToken(
            @PathVariable String token,
            HttpServletRequest request) {

        String ip = resolveClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        Object result = publicViewService.handleView(token, ip, userAgent);
        return ResponseEntity.ok(result);
    }

    // =====================================================
    // VERIFY PIN
    // =====================================================
    @PostMapping("/{token}/verify-pin")
    @ApiMessage("Xác minh PIN để xem quy trình")
    public ResponseEntity<?> verifyPin(
            @PathVariable String token,
            @Valid @RequestBody VerifyPinRequest req,
            HttpServletRequest request) {

        String ip = resolveClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        Object result = publicViewService.handleVerifyPin(token, req.getPin(), ip, userAgent);
        return ResponseEntity.ok(result);
    }

    // =====================================================
    // HELPER: lấy IP thật của client (qua proxy/nginx)
    // =====================================================
    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            // X-Forwarded-For có thể chứa nhiều IP: "client, proxy1, proxy2"
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}