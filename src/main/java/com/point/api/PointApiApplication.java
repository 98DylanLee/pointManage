package com.point.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PointApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PointApiApplication.class, args);
    }
}
