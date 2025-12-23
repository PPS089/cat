package com.example.petweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.socket.config.annotation.EnableWebSocket; //开启注解方式的事务管理



@EnableTransactionManagement //开启注解方式的事务管理
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.petweb", "com.example.petcommon", "com.example.petservice"})
@EnableWebSocket
@EnableScheduling
public class PetWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetWebApplication.class, args);
    }

}