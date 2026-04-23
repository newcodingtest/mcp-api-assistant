package com.example.swaggerservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI test1OpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Test1 API")
                        .description("테스트 서비스1용 OpenAPI 문서")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("JY Team")
                                .email("dev@example.com")));
    }
}
