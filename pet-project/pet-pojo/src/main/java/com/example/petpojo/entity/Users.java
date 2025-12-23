package com.example.petpojo.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



/**
 * 用户实体类
 * 使用MyBatis-Plus官方自动填充注解
 * @author 33185
 */
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@TableName("users")
@Schema(description = "用户实体")
public class Users implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "用户ID")
    private Integer id;
    @TableField(value = "username")
    @Schema(description = "用户名")
    private String userName;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "密码哈希", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String passwordHash;
    @Schema(description = "个人简介")
    private String introduce;
    @TableField(value = "headpic")
    @Schema(description = "头像URL")
    private String headPic;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "角色")
    private String role;

    @TableField(value = "admin_shelter_id")
    @Schema(description = "管理员绑定的收容所ID，普通用户为null")
    private Integer adminShelterId;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
