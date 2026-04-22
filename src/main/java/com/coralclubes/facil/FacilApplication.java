package com.coralclubes.facil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FacilApplication {

    public static void main(String[] args) {
        SpringApplication.run(FacilApplication.class, args);
    }

}
