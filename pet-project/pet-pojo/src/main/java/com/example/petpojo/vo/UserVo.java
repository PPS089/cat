package com.example.petpojo.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVo {

    private Long userId;

    private String userName;

    private String phone;

    private String introduce;

    private String headPic;

    private String email;

}
