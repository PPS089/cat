package com.example.petproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.example.petcommon.properties.AliyunOSSProperties;

@SpringBootApplication
@EnableConfigurationProperties({AliyunOSSProperties.class})
public class PetProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetProjectApplication.class, args);
    }

}