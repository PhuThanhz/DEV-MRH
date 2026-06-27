package vn.system.app.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PermissionInterceptorConfiguration implements WebMvcConfigurer {
    @Bean
    PermissionInterceptor getPermissionInterceptor() {
        return new PermissionInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] whiteList = {
                "/", "/api/v1/auth/**", "/storage/**", "/api/v1/email/**",
                "/api/v1/files", "/api/v1/files/**", "/uploads/procedures/**", "/uploads/**", "/api/public/view/**",
                "/api/v1/document-categories/active",
                "/api/v1/accounting-document-categories/active",
                // --- PROFILE ---
                "/api/v1/users/profile",
                // --- EVALUATION: Notifications (Skipping Permission Check) ---
                "/api/v1/evaluation/notifications/**",
                "/api/v1/notifications/**", "/api/v1/notifications",
                "/api/v1/ws-endpoint/**", "/api/v1/ws-endpoint"
        };
        registry.addInterceptor(getPermissionInterceptor())
                .excludePathPatterns(whiteList);
    }
}
