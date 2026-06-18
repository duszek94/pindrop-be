package com.duszek.pindrop.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(name = "io.swagger.v3.oas.models.OpenAPI")
public class OpenApiConfig {

	@Bean
	public OpenAPI pindropOpenApi() {
		final String bearerScheme = "bearerAuth";
		return new OpenAPI()
				.info(new Info()
						.title("Pindrop API")
						.version("v1")
						.description("Pindrop TravelTech backend API"))
				.addSecurityItem(new SecurityRequirement().addList(bearerScheme))
				.components(new Components()
						.addSecuritySchemes(bearerScheme, new SecurityScheme()
								.name(bearerScheme)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")));
	}
}
