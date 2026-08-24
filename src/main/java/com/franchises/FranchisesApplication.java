package com.franchises;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(
        title = "Franchises API",
        version = "1.0.0",
        description = "API reactiva para la gestión de franquicias, sus sucursales "
                + "y los productos ofertados en cada sucursal."))
@SpringBootApplication
public class FranchisesApplication {

    public static void main(String[] args) {
        SpringApplication.run(FranchisesApplication.class, args);
    }
}
