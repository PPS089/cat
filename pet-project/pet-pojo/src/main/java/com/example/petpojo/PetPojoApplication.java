package com.example.petpojo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class PetPojoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetPojoApplication.class, args);
    }

}
