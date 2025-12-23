package com.example.petpojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应VO")
public class UserLoginVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "用户名")
    private String username;


    @Schema(description = "访问Token")
    private String accessToken;

    @Schema(description = "刷新Token")
    private String refreshToken;
    
    @Schema(description = "过期时间（毫秒时间戳）")
    private Long expireTime;

    @Schema(description = "角色（ADMIN/USER）")
    private String role;
    
    @Schema(description = "管理员绑定的收容所ID，null表示平台管理员")
    private Integer adminShelterId;
    
    @Schema(description = "提示信息")
    private String message;
    
    @Schema(description = "是否成功")
    private boolean ok;
}
