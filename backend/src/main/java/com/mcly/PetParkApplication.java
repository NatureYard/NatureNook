package com.mcly;

import com.mcly.common.config.GateProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(GateProperties.class)
public class PetParkApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetParkApplication.class, args);
    }
}

