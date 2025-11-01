package com.example.petservice;

import com.example.petcommon.config.MyBatisPlusConfig;
import com.example.petcommon.config.SecurityConfig;
import com.example.petcommon.config.JwtProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({MyBatisPlusConfig.class, SecurityConfig.class, JwtProperties.class})
class PetServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}