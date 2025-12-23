package com.example.petpojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author 33185
 */
@Data
@Schema(description = "修改密码DTO")
public class ChangePasswordDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 当前密码
     */
    @NotBlank(message = "当前密码不能为空")
    @Size(min = 6, max = 12, message = "当前密码长度必须在6-12位之间")
    @Schema(description = "当前密码")
    private String currentPassword;

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 12, message = "新密码长度必须在6-12位之间")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "新密码必须包含大小写字母和数字")
    @Schema(description = "新密码")
    private String newPassword;
}
