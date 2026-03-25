package vn.system.app.modules.userinfo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.userinfo.domain.request.ReqUserInfoDTO;
import vn.system.app.modules.userinfo.domain.response.ResUserInfoDTO;
import vn.system.app.modules.userinfo.service.UserInfoService;

@RestController
@RequestMapping("/api/v1/users")
public class UserInfoController {

    private final UserInfoService userInfoService;

    public UserInfoController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    // ======================================================
    // CREATE
    // ======================================================
    @PostMapping("/{userId}/info")
    @ApiMessage("Tạo thông tin cá nhân cho user")
    public ResponseEntity<ResUserInfoDTO> create(
            @PathVariable Long userId,
            @Valid @RequestBody ReqUserInfoDTO req) {

        ResUserInfoDTO res = userInfoService.handleCreate(userId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // ======================================================
    // UPDATE
    // ======================================================
    @PutMapping("/{userId}/info")
    @ApiMessage("Cập nhật thông tin cá nhân của user")
    public ResponseEntity<ResUserInfoDTO> update(
            @PathVariable Long userId,
            @Valid @RequestBody ReqUserInfoDTO req) {

        ResUserInfoDTO res = userInfoService.handleUpdate(userId, req);
        return ResponseEntity.ok(res);
    }

    // ======================================================
    // GET BY USER
    // ======================================================
    @GetMapping("/{userId}/info")
    @ApiMessage("Lấy thông tin cá nhân của user")
    public ResponseEntity<ResUserInfoDTO> getByUser(
            @PathVariable Long userId) {

        ResUserInfoDTO res = userInfoService.fetchByUserId(userId);
        return ResponseEntity.ok(res);
    }
}