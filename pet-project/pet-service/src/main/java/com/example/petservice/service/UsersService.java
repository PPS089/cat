package com.example.petservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.petcommon.result.UserLoginResponse;
import com.example.petpojo.dto.ChangePasswordDto;
import com.example.petpojo.dto.UserUpdateDto;
import com.example.petpojo.dto.UsersCreateDto;
import com.example.petpojo.entity.Users;
import com.example.petpojo.dto.UserLoginDto;
import com.example.petpojo.vo.UserVo;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;


public interface UsersService extends IService<Users> {
    /**
     * 创建用户
     */
    UserLoginResponse createUser(UsersCreateDto userdto);
    /**
     * 用户登录
     */
    UserLoginResponse userLogin(UserLoginDto userLoginDto);
    
    /**
     * 用户登录（带请求信息）
     */
    UserLoginResponse userLogin(UserLoginDto userLoginDto, HttpServletRequest request);
    /**
     * 更新用户信息
     */
    UserVo updateUser(UserUpdateDto userUpdateDto, MultipartFile file);
    /**
     * 更改密码
     */
    boolean changePassword(ChangePasswordDto changePasswordDto);
    /**
     * 获取用户个人信息
     */
    UserVo getUserProfile();

    /**
     * 刷新JWT token
     */
    UserLoginResponse refreshToken(HttpServletRequest request);

}