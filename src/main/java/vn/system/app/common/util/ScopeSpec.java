package vn.system.app.common.util;

import org.springframework.data.jpa.domain.Specification;

public class ScopeSpec {

    /**
     * Filter theo công ty mà user hiện tại có quyền truy cập.
     *
     * Cách dùng:
     * - CompanyController → ScopeSpec.byCompanyScope("id")
     * - DepartmentController → ScopeSpec.byCompanyScope("company.id")
     * - SectionController → ScopeSpec.byCompanyScope("department.company.id")
     * - UserController → ScopeSpec.byCompanyScope("company.id")
     */
    @SuppressWarnings("unchecked")
    public static <T> Specification<T> byCompanyScope(String fieldPath) {

        UserScopeContext.UserScope scope = UserScopeContext.get();

        // Chưa có scope (request từ whitelist) → không filter gì
        if (scope == null) {
            return Specification.where(null);
        }

        // SUPER_ADMIN hoặc isAdminLevel → thấy hết, không filter
        if (scope.isSuperAdmin() || scope.isAdminLevel()) {
            return Specification.where(null);
        }

        // Không có công ty nào → không cho thấy gì
        if (scope.companyIds() == null || scope.companyIds().isEmpty()) {
            return (root, query, cb) -> cb.disjunction();
        }

        // User thường → WHERE <fieldPath> IN (companyIds)
        return (root, query, cb) -> {
            String[] parts = fieldPath.split("\\.");
            jakarta.persistence.criteria.Path<?> path = root.get(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                path = path.get(parts[i]);
            }
            return path.in(scope.companyIds());
        };
    }

    /**
     * Filter theo phòng ban mà user hiện tại có quyền truy cập. ← THÊM
     *
     * Cách dùng:
     * - DepartmentController → ScopeSpec.byDepartmentScope("id")
     * - SectionController → ScopeSpec.byDepartmentScope("department.id")
     * - DepartmentJobTitle → ScopeSpec.byDepartmentScope("department.id")
     */
    @SuppressWarnings("unchecked")
    public static <T> Specification<T> byDepartmentScope(String fieldPath) {

        UserScopeContext.UserScope scope = UserScopeContext.get();

        // Chưa có scope → không filter gì
        if (scope == null) {
            return Specification.where(null);
        }

        // SUPER_ADMIN hoặc isAdminLevel → thấy hết, không filter
        if (scope.isSuperAdmin() || scope.isAdminLevel()) {
            return Specification.where(null);
        }

        // Không có phòng ban nào → không cho thấy gì
        if (scope.departmentIds() == null || scope.departmentIds().isEmpty()) {
            return (root, query, cb) -> cb.disjunction();
        }

        // User thường → WHERE <fieldPath> IN (departmentIds)
        return (root, query, cb) -> {
            String[] parts = fieldPath.split("\\.");
            jakarta.persistence.criteria.Path<?> path = root.get(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                path = path.get(parts[i]);
            }
            return path.in(scope.departmentIds());
        };
    }
}