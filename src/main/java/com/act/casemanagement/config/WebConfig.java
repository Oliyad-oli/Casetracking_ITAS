package com.act.casemanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration including CORS for frontend integration.
 */
@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/v1/**")
                        .allowedOrigins(
                                "http://localhost:3000",
                                "http://localhost:5173",
                                "http://localhost:5174",
                                "http://localhost:4200",
                                "http://127.0.0.1:3000",
                                "http://127.0.0.1:5173",
                                "http://127.0.0.1:5174",
                                "http://127.0.0.1:4200"
                        )
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders(
                                "Content-Type",
                                "Authorization",
                                "X-Authenticated-Actor-Id",
                                "X-Authenticated-Role",
                                "X-Authenticated-Unit-Id",
                                "X-Idempotency-Key"
                        )
                        .exposedHeaders("Location", "X-Total-Count")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}
