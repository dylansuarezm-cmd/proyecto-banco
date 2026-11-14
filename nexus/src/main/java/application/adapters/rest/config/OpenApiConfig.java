package application.adapters.rest.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI definition. The "Authorize" button asks for identifier and password (HTTP Basic).
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "NexusMarket API", version = "1.0",
                description = "Marketplace: users, catalog, inventory, orders, returns and refunds."),
        security = @SecurityRequirement(name = "basicAuth"))
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic")
public class OpenApiConfig {
}
