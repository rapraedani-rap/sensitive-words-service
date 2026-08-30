package za.co.flash.sensitivewords.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info().title("Sensitive Words API").version("1.0.0")
                        .description("REST API for managing sensitive words and sanitizing text " +
                                        "by masking configured sensitive words.")
                        .contact(new Contact().name("Flash")));
    }
}