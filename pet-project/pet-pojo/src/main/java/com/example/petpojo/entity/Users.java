package com.example.petpojo.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



/**
 * 用户实体类
 * 使用MyBatis-Plus官方自动填充注解
 */
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@TableName("users")
public class Users implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField(value = "username")
    private String userName;
    private String phone;
    private String passwordHash;
    private String introduce;
    @TableField(value = "headpic")
    private String headPic;
    private String email;
    
    @TableField(value = "created_at", fill=FieldFill.INSERT)
    private LocalDateTime createAt;
}