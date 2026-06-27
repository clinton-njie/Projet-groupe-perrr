package com.immobilier.reservationservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Configuration pour Swagger/OpenAPI, 
// définissant les informations de base de l'API pour la documentation
@Configuration
public class SwaggerConfig {

    // Bean pour la configuration de l'OpenAPI, 
    // définissant les informations de base de l'API
    @Bean
    public OpenAPI openAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Reservation Service")
                        .version("1.0")
                        .description(
                                "Micro service de gestion des reservations."));
    }
}