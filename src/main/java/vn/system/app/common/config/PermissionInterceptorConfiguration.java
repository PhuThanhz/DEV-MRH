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
                "/api/v1/files", "/api/v1/position-levels/**",
                "/api/v1/job-titles/**", "/api/v1/salary-grades/**", "/api/v1/org-job-titles/**",
                "/api/v1/salary-grade-performance-ratings/**", "/api/v1/salary-grade-incomes/**",
                "/api/v1/process-actions/**",
                "/api/v1/permission-category-scopes/**",
                "/api/v1/permission-categories/**", "/api/v1/company-job-titles/**",

                "/api/v1/permission-contents/**",
                "/api/v1/permission-contents/**",
                "/api/v1/permission-categories/**", "/api/v1/dept-missions/**",
                "/api/v1/company-salary-grades/**",
                "/api/v1/department-salary-grades/**",
                "/api/v1/section-salary-grades/**", "/api/v1/job-title-performance-contents/**",
                "/api/v1/organizations/**", "/api/v1/positions/**", "/api/v1/permission-matrix/**",
                "/api/v1/users/profile",
                "/api/v1/files/**", "/api/v1/users/*/positions",
                "/api/v1/users/positions/*", "/api/v1/users/request-password-code", // ← có chưa?
                "/api/v1/users/confirm-reset-password",

                "/api/v1/users/by-company/**", "/api/v1/employee-career-paths/**", "/api/v1/career-path-templates/**",

        };
        registry.addInterceptor(getPermissionInterceptor())
                .excludePathPatterns(whiteList);
    }
}
