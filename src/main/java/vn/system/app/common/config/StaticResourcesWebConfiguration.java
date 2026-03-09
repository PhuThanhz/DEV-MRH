package vn.system.app.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourcesWebConfiguration implements WebMvcConfigurer {

    @Value("${lotusgroup.upload-file.dir}")
    private String uploadDir;

    @Value("${lotusgroup.upload-file.public-url}")
    private String publicUrl;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(publicUrl + "/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}