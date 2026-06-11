package com.brainbooster.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    private static final String AUTHOR = "chrisitstyle";

    @Bean
    public OpenAPI brainBoosterOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Brain Booster API")
                        .description("REST API for Brain Booster flashcard learning application.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name(AUTHOR)
                                .url("https://github.com/chrisitstyle"))
                        .license(new License()
                                .name("unknown")))
                .addServersItem(new Server()
                        .url("/api/v1")
                        .description("Default API server"))
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ))
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME));
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("auth")
                .pathsToMatch("/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi usersApi() {
        return GroupedOpenApi.builder()
                .group("users")
                .pathsToMatch("/users/**")
                .build();
    }

    @Bean
    public GroupedOpenApi foldersApi() {
        return GroupedOpenApi.builder()
                .group("folders")
                .pathsToMatch("/folders/**")
                .build();
    }

    @Bean
    public GroupedOpenApi flashcardSetsApi() {
        return GroupedOpenApi.builder()
                .group("flashcard-sets")
                .pathsToMatch("/flashcard-sets/**")
                .build();
    }

    @Bean
    public GroupedOpenApi gameResultsApi() {
        return GroupedOpenApi.builder()
                .group("game-results")
                .pathsToMatch("/game-results/**")
                .build();
    }

    @Bean
    public GroupedOpenApi gameAttemptsApi() {
        return GroupedOpenApi.builder()
                .group("game-attempts")
                .pathsToMatch("/game-attempts/**")
                .build();
    }

}
