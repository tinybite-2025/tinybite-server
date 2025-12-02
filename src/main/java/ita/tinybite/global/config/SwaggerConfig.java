package ita.tinybite.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "BearerAuth",
                                        new SecurityScheme()
                                                .name("Authorization")
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .in(SecurityScheme.In.HEADER)))
                .info(
                        new Info()
                                .title("TinyBite Swagger Page")
                                .license(
                                        new License()
                                                .name("tinybite API 명세서")
                                                .url("http://localhost:8080")));
    }
}
