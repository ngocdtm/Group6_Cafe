package com.coffee.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "Cafe Management System",
                description = "API documentation for managing cafe operations",
                summary = "Documentation of all APIs for the Cafe Management System",
                termsOfService = "Terms of service for using the Cafe Management System API",
                contact = @Contact(
                        name = "Cafe Management Support",
                        email = "ngocdtm@cafemanagement.com"
                ),
                version = "v1.0"
        ),
        servers = {
                @Server(
                        description = "Local environment",
                        url = "http://localhost:8080"
                ),
                @Server(
                        description = "Development environment",
                        url = "http://dev.cafemanagement.com"
                ),
                @Server(
                        description = "Testing environment",
                        url = "http://test.cafemanagement.com"
                ),
                @Server(
                        description = "Acceptance environment",
                        url = "http://acc.cafemanagement.com"
                ),
                @Server(
                        description = "Production environment",
                        url = "http://prod.cafemanagement.com"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        in = SecuritySchemeIn.HEADER,
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = "JWT authentication for the Cafe Management System API"
)
public class OpenApiConfig {
}