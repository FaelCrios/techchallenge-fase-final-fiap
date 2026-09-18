package br.com.fiap.filacerta.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI filaCertaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("FilaCerta SUS API")
                        .version("v1")
                        .description("API do MVP para otimização de filas e reaproveitamento de vagas no SUS."));
    }
}
