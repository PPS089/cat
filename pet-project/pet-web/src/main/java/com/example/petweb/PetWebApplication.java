package com.example.petweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.petweb", "com.example.petservice", "com.example.petcommon"})
public class PetWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetWebApplication.class, args);
    }

}
