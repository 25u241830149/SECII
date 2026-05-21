package com.campushub.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final Path uploadBasePath;
    private final String publicPrefix;

    public WebMvcConfig(
            @Value("${campushub.file.upload-base-path:./uploads}") String uploadBasePath,
            @Value("${campushub.file.public-prefix:/uploads}") String publicPrefix
    ) {
        this.uploadBasePath = Paths.get(uploadBasePath).toAbsolutePath().normalize();
        this.publicPrefix = normalizePublicPrefix(publicPrefix);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(publicPrefix + "/**")
                .addResourceLocations(uploadBasePath.toUri().toString());
    }

    private static String normalizePublicPrefix(String value) {
        String prefix = value == null || value.isBlank() ? "/uploads" : value.trim();
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        while (prefix.endsWith("/") && prefix.length() > 1) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return prefix;
    }
}
