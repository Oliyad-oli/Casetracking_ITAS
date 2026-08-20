package com.act.casemanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI caseManagementOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("RCMIS Case Management Service")
                        .description("ITAS Case Management — CTR0100 through CTR1100. " +
                                "Authentication via API Gateway (Keycloak). " +
                                "Pass X-Authenticated-Actor-Id, X-Authenticated-Role, " +
                                "X-Authenticated-Unit-Id headers.")
                        .version("1.0.0"))
                .addServersItem(new Server().url("http://localhost:8082/api/v1")
                        .description("Local development"));
    }
}
