package com.example.petcommon.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginResponse {
    
    private Long userId;
    
    private String username;
    
    private String token;
    
    private Long expireTime;
    
    private String message;
    
    private boolean ok;
}