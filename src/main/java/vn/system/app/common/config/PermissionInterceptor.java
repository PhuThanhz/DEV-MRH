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
import vn.system.app.modules.adminscope.service.UserAdminScopeService;
import vn.system.app.modules.permission.domain.Permission;
import vn.system.app.modules.role.domain.Role;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.service.UserService;

public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    UserService userService;

    @Autowired
    UserAdminScopeService userAdminScopeService;

    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();

    // SUPER_ADMIN, ADMIN_SUB_1 → thấy toàn bộ, không filter
    private static final List<String> FULL_COMPANY_ROLES = List.of(
            "SUPER_ADMIN",
            "ADMIN_SUB_1");

    // ADMIN_SUB_2 → admin cấp công ty, filter theo companyIds từ UserPosition
    private static final List<String> COMPANY_LEVEL_ROLES = List.of(
            "ADMIN_SUB_2");

    // DEPARTMENT_MANAGER, ADMIN_SUB_3 → filter theo phòng ban quản lý
    private static final List<String> DEPARTMENT_LEVEL_ROLES = List.of(
            "DEPARTMENT_MANAGER",
            "ADMIN_SUB_3");

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
                String ip = request.getHeader("X-Forwarded-For");
                if (ip != null && !ip.isBlank()) {
                    ip = ip.split(",")[0].trim();
                } else {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isBlank()) {
                    ip = request.getRemoteAddr();
                }
                
                userService.updateLastLoginIfNecessary(user.getId(), ip);

                // ── SET SCOPE vào ThreadLocal ────────────────────────────
                String roleName = user.getRole() != null ? user.getRole().getName() : "";

                boolean isSuperAdmin = "SUPER_ADMIN".equals(roleName);

                // isAdminLevel = true → SUPER_ADMIN, ADMIN_SUB_1: thấy toàn bộ, không filter
                boolean isAdminLevel = FULL_COMPANY_ROLES.contains(roleName);

                // isCompanyLevel = true → ADMIN_SUB_2: thấy toàn bộ công ty được gán
                boolean isCompanyLevel = COMPANY_LEVEL_ROLES.contains(roleName);

                // isDepartmentLevel = true → DEPARTMENT_MANAGER: filter theo phòng ban được gán
                boolean isDepartmentLevel = DEPARTMENT_LEVEL_ROLES.contains(roleName);

                Set<Long> companyIds = Set.of();
                Set<Long> departmentIds = Set.of();

                if (isCompanyLevel) {
                    companyIds = userAdminScopeService.getCompanyScopeIds(user.getId());
                } else if (isDepartmentLevel) {
                    departmentIds = userAdminScopeService.getDepartmentScopeIds(user.getId(), roleName);
                    companyIds = userAdminScopeService.getCompanyIdsFromDepartmentScopes(user.getId(), roleName);
                }

                UserScopeContext.set(new UserScopeContext.UserScope(
                        user.getId(),
                        companyIds,
                        departmentIds,
                        isSuperAdmin,
                        isAdminLevel,
                        isCompanyLevel,
                        isDepartmentLevel));

                Role role = user.getRole();
                if (role != null) {

                    // SUPER_ADMIN bỏ qua check permission
                    if (isSuperAdmin) {
                        return true;
                    }

                    List<Permission> permissions = role.getPermissions();
                    boolean isAllow = permissions.stream().anyMatch(item -> {
                        boolean methodMatch = "*".equals(item.getMethod()) || item.getMethod().equalsIgnoreCase(httpMethod);
                        boolean pathMatch = false;

                        if (path != null && !path.isBlank()) {
                            // 1. So khớp chính xác mẫu Controller của Spring Boot (ví dụ: /api/v1/evaluation/templates/{id})
                            pathMatch = item.getApiPath().equals(path);

                            // 2. Nếu không khớp chính xác nhưng cấu hình có wildcard (* hoặc **), dùng AntPathMatcher
                            if (!pathMatch && (item.getApiPath().contains("*") || item.getApiPath().contains("**"))) {
                                try {
                                    pathMatch = antPathMatcher.match(item.getApiPath(), path);
                                } catch (Exception e) {
                                    // ignore lỗi pattern
                                }
                            }
                        } else {
                            // Fallback nếu không có BEST_MATCHING_PATTERN_ATTRIBUTE
                            try {
                                pathMatch = antPathMatcher.match(item.getApiPath(), requestURI);
                            } catch (Exception e) {
                                // ignore lỗi pattern
                            }
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
