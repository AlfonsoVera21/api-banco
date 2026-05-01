package com.api.apibanco.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiBancoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Banco")
                        .description("Documentacion de servicios para clientes, cuentas y movimientos.")
                        .version("v1"))
                .addServersItem(new Server()
                        .url("/")
                        .description("Servidor actual"));
    }
}
