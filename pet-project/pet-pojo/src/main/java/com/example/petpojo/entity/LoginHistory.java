package com.example.petpojo.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.petpojo.entity.enums.CommonEnum.LoginStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 33185
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("login_history")
@Schema(description = "登录历史实体")
public class LoginHistory  implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "记录ID")
    private Integer id;

    @NotNull(message = "用户ID不能为空")
    @Min(value = -1, message = "用户ID不能小于-1")
    @Schema(description = "用户ID（-1表示未知用户）")
    private Integer userId;

    @TableField(value = "login_time", fill = FieldFill.INSERT)
    @NotNull(message = "登录时间不能为空")
    @Schema(description = "登录时间")
    private LocalDateTime loginTime;
    
    @NotBlank(message = "IP 地址不能为空")
    @Size(max = 64, message = "IP 地址长度不能超过64字符")
    @Schema(description = "IP地址")
    private String ipAddress;
    @Schema(description = "User-Agent")
    @Size(max = 512, message = "User-Agent 长度不能超过512字符")
    private String userAgent;
    @Schema(description = "设备信息")
    @Size(max = 255, message = "设备信息长度不能超过255字符")
    private String device;
    @Schema(description = "位置")
    @Size(max = 255, message = "位置描述长度不能超过255字符")
    private String location;
    @NotNull(message = "状态不能为空")
    @Schema(description = "登录状态")
    private LoginStatusEnum status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @PastOrPresent(message = "创建时间不能晚于当前时间")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @PastOrPresent(message = "更新时间不能晚于当前时间")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
