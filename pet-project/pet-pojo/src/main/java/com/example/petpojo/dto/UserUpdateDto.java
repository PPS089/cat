package com.example.petpojo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;




/**
 * 用户更新DTO
 * 后期改一下前端字段
 */
@Data
public class UserUpdateDto {

    private String userName;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String introduce;

    @Email(message = "邮箱格式不正确")
    private String email;

}



//
//@TableId(type = IdType.AUTO)
//private Integer id;
//@TableField(value = "username")
//private String userName;
//private String phone;
//private String passwordHash;
//private String introduce;
//@TableField(value = "nickname")
//private String nickName;
//@TableField(value = "headpic")
//private String headPic;
//private String email;