package com.nutrivida.nutrivida_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

// Swagger UI: http://<host>:8080/swagger-ui.html
// El botón "Authorize" permite pegar el accessToken para probar las rutas protegidas.
@Configuration
public class OpenApiConfig {

    public static final String BEARER = "bearerAuth";

    @Bean
    OpenAPI nutrividaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("NutriVida API")
                        .version("v1")
                        .description("API REST de la Clínica Nutricional NutriVida: usuarios, autenticación con JWT y roles."))
                .components(new Components().addSecuritySchemes(BEARER,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
