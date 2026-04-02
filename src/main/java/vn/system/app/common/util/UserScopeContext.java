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
            Long userId,
            Set<Long> companyIds,
            boolean isSuperAdmin) {
    }
}