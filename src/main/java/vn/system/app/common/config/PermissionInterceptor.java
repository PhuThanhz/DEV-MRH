package vn.system.app.common.config;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;
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

    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();

    // SUPER_ADMIN, ADMIN_SUB_1 → thấy toàn bộ, không filter
    private static final List<String> FULL_COMPANY_ROLES = List.of(
            "SUPER_ADMIN",
            "ADMIN_SUB_1");

    // ADMIN_SUB_2 → admin cấp công ty, filter theo companyIds từ UserPosition
    private static final List<String> COMPANY_LEVEL_ROLES = List.of(
            "ADMIN_SUB_2");

    @Override
    @Transactional
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response, Object handler)
            throws Exception {

        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();

        String email = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        if (email != null && !email.isEmpty()) {
            User user = this.userService.handleGetUserByUsername(email);

            if (user != null && !user.isActive()) {
                throw new PermissionException("Tài khoản đã bị vô hiệu hóa");
            }

            if (user != null) {

                // ── Cập nhật lastLoginAt + IP (tối đa mỗi 10 phút) ──────
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

                // ── SET SCOPE vào ThreadLocal ────────────────────────────
                String roleName = user.getRole() != null ? user.getRole().getName() : "";

                boolean isSuperAdmin = "SUPER_ADMIN".equals(roleName);

                // isAdminLevel = true → SUPER_ADMIN, ADMIN_SUB_1: thấy toàn bộ, không filter
                boolean isAdminLevel = FULL_COMPANY_ROLES.contains(roleName);

                // isCompanyLevel = true → ADMIN_SUB_2: thấy toàn bộ công ty được gán
                boolean isCompanyLevel = COMPANY_LEVEL_ROLES.contains(roleName);

                // companyIds: chỉ cần lấy nếu không phải admin toàn hệ thống
                Set<Long> companyIds = isAdminLevel
                        ? Set.of()
                        : userPositionService.getCompanyIdsByUser(user.getId());

                // departmentIds: ADMIN_SUB_2 không cần filter theo phòng ban
                Set<Long> departmentIds = (isAdminLevel || isCompanyLevel)
                        ? Set.of()
                        : userPositionService.getDepartmentIdsByUser(user.getId());

                UserScopeContext.set(new UserScopeContext.UserScope(
                        user.getId(),
                        companyIds,
                        departmentIds,
                        isSuperAdmin,
                        isAdminLevel,
                        isCompanyLevel));
                // ─────────────────────────────────────────────────────────

                Role role = user.getRole();
                if (role != null) {

                    // SUPER_ADMIN bỏ qua check permission
                    if (isSuperAdmin) {
                        return true;
                    }

                    List<Permission> permissions = role.getPermissions();
                    boolean isAllow = permissions.stream().anyMatch(item -> {
                        boolean methodMatch = item.getMethod().equalsIgnoreCase(httpMethod);
                        boolean pathMatch = false;

                        try {
                            pathMatch = antPathMatcher.match(item.getApiPath(), requestURI);
                        } catch (Exception e) {
                            // ignore lỗi pattern không hợp lệ
                        }

                        if (!pathMatch) {
                            pathMatch = item.getApiPath().equals(path);
                        }

                        return methodMatch && pathMatch;
                    });

                    if (!isAllow) {
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