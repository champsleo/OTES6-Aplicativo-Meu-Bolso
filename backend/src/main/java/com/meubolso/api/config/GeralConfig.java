package com.meubolso.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

@Configuration
@EnableScheduling
public class GeralConfig {

    // Os services recebem o relógio por injeção (em vez de LocalDate.now()) para os testes poderem fixar o "hoje".
    @Bean
    public Clock relogio() {
        return Clock.systemDefaultZone();
    }
}
