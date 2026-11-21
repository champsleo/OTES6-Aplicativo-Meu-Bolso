package com.meubolso.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// O login é feito por JWT, então desligamos o usuário e senha padrão que o Spring Security cria.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class MeuBolsoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeuBolsoApplication.class, args);
    }
}
