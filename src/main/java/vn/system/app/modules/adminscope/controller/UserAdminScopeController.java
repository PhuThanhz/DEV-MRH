package vn.system.app.modules.adminscope.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.adminscope.domain.request.ReqUpsertUserAdminScopesDTO;
import vn.system.app.modules.adminscope.domain.response.ResUserAdminScopeDTO;
import vn.system.app.modules.adminscope.service.UserAdminScopeService;

@RestController
@RequestMapping("/api/v1/users")
public class UserAdminScopeController {

    private final UserAdminScopeService service;

    public UserAdminScopeController(UserAdminScopeService service) {
        this.service = service;
    }

    @GetMapping("/{userId}/admin-scopes")
    @ApiMessage("Danh sách phạm vi quản trị của user")
    public ResponseEntity<List<ResUserAdminScopeDTO>> getByUser(@PathVariable String userId) {
        return ResponseEntity.ok(service.fetchByUser(userId));
    }

    @PutMapping("/{userId}/admin-scopes")
    @ApiMessage("Cập nhật phạm vi quản trị của user")
    public ResponseEntity<List<ResUserAdminScopeDTO>> replaceScopes(
            @PathVariable String userId,
            @Valid @RequestBody ReqUpsertUserAdminScopesDTO req) {
        return ResponseEntity.ok(service.replaceScopes(userId, req));
    }
}
