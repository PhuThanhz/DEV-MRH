package vn.system.app.common.config;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.common.util.UserScopeContext;
import vn.system.app.common.util.error.PermissionException;
import vn.system.app.modules.permission.domain.Permission;
import vn.system.app.modules.role.domain.Role;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.service.UserService;
import vn.system.app.modules.userposition.service.UserPositionService;

public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    UserService userService;

    @Autowired
    UserPositionService userPositionService;

    @Override
    @Transactional
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response, Object handler)
            throws Exception {

        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();
        System.out.println(">>> RUN preHandle");
        System.out.println(">>> path= " + path);
        System.out.println(">>> httpMethod= " + httpMethod);
        System.out.println(">>> requestURI= " + requestURI);

        String email = SecurityUtil.getCurrentUserLogin().isPresent() == true
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        if (email != null && !email.isEmpty()) {
            User user = this.userService.handleGetUserByUsername(email);
            if (user != null && !user.isActive()) {
                throw new PermissionException("Tài khoản đã bị vô hiệu hóa");
            }
            if (user != null) {

                Instant now = Instant.now();
                boolean shouldUpdate = user.getLastLoginAt() == null
                        || user.getLastLoginAt().isBefore(now.minus(10, ChronoUnit.MINUTES));

                if (shouldUpdate) {
                    String ip = request.getHeader("X-Forwarded-For");

                    if (ip != null && !ip.isBlank()) {
                        ip = ip.split(",")[0].trim();
                    } else {
                        ip = request.getHeader("X-Real-IP");
                    }

                    if (ip == null || ip.isBlank()) {
                        ip = request.getRemoteAddr();
                    }
                    user.setLastLoginAt(now);
                    user.setLastLoginIp(ip);
                    userService.save(user);
                }
                // ── SET SCOPE vào ThreadLocal ──────────────────────────────
                boolean isSuperAdmin = user.getRole() != null
                        && "SUPER_ADMIN".equals(user.getRole().getName());

                // ← THÊM: role được xem hết data như SUPER_ADMIN
                boolean isFullViewRole = user.getRole() != null
                        && List.of("SUPER_ADMIN", "ADMIN_SUB_1") // ← đổi "ADMIN_SUB_1" thành tên role đúng trong DB
                                .contains(user.getRole().getName());

                // ← ĐỔI: dùng isFullViewRole thay vì isSuperAdmin
                Set<Long> companyIds = isFullViewRole
                        ? Set.of()
                        : userPositionService.getCompanyIdsByUser(user.getId());

                // ← ĐỔI: truyền isFullViewRole thay vì isSuperAdmin
                UserScopeContext.set(new UserScopeContext.UserScope(
                        user.getId(), companyIds, isFullViewRole));
                // ───────────────────────────────────────────────────────────

                Role role = user.getRole();
                if (role != null) {

                    // SUPER_ADMIN bỏ qua check permission — KHÔNG ĐỔI
                    if ("SUPER_ADMIN".equals(role.getName())) {
                        return true;
                    }

                    List<Permission> permissions = role.getPermissions();
                    boolean isAllow = permissions.stream().anyMatch(item -> item.getApiPath().equals(path)
                            && item.getMethod().equals(httpMethod));

                    if (isAllow == false) {
                        throw new PermissionException("Bạn không có quyền truy cập endpoint này.");
                    }
                } else {
                    throw new PermissionException("Bạn không có quyền truy cập endpoint này.");
                }
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler, Exception ex) {
        UserScopeContext.clear();
    }
}