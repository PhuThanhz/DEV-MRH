package vn.system.app.common.util;

import java.util.Set;

public class UserScopeContext {

    private static final ThreadLocal<UserScope> holder = new ThreadLocal<>();

    public static void set(UserScope scope) {
        holder.set(scope);
    }

    public static UserScope get() {
        return holder.get();
    }

    public static void clear() {
        holder.remove();
    }

    public record UserScope(
            String userId,
            Set<Long> companyIds,
            Set<Long> departmentIds, // ← dùng cho filter theo phòng ban
            boolean isSuperAdmin,
            boolean isAdminLevel, // ← true nếu được thấy toàn bộ (SUPER_ADMIN, ADMIN_SUB_1)
            boolean isCompanyLevel // ← THÊM: true nếu là ADMIN_SUB_2 (thấy toàn bộ công ty được gán)
    ) {
    }
}