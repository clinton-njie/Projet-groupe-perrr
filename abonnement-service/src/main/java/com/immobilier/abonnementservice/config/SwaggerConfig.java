package com.immobilier.abonnementservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI abonnementAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("ABONNEMENT-SERVICE API")
                        .version("1.0")
                        .description("Gestion des abonnements des agents"));
    }

}