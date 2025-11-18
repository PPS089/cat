package com.example.petpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginVo {
    
    private Long userId;
    
    private String username;


    private String accessToken;

    private String refreshToken;
    
    private Long expireTime;
    
    private String message;
    
    private boolean ok;
}