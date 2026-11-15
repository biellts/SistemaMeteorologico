package br.edu.meteorologia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SistemaMeteorologicoApplication {

    public static void main(String[] args) {

        SpringApplication.run(
                SistemaMeteorologicoApplication.class,
                args
        );
    }
}
