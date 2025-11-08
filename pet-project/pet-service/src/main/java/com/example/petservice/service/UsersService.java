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


/**
 * 用户服务接口
 * 定义用户相关的业务方法
 */
public interface UsersService extends IService<Users> {
    /**
     * 创建用户
     * @param userdto 用户创建信息
     * @return 用户登录响应信息
     */
    UserLoginResponse createUser(UsersCreateDto userdto);
    
    /**
     * 用户登录
     * @param userLoginDto 用户登录信息
     * @return 用户登录响应信息
     */
    UserLoginResponse userLogin(UserLoginDto userLoginDto);
    
    /**
     * 用户登录（带请求信息）
     * @param userLoginDto 用户登录信息
     * @param request HTTP请求对象
     * @return 用户登录响应信息
     */
    UserLoginResponse userLogin(UserLoginDto userLoginDto, HttpServletRequest request);
    
    /**
     * 更新用户信息
     * @param userUpdateDto 用户更新信息
     * @param file 用户头像文件
     * @return 用户VO对象
     */
    UserVo updateUser(UserUpdateDto userUpdateDto, MultipartFile file);
    
    /**
     * 更改密码
     * @param changePasswordDto 密码修改信息
     * @return 是否修改成功
     */
    boolean changePassword(ChangePasswordDto changePasswordDto);
    
    /**
     * 获取用户个人信息
     * @return 用户VO对象
     */
    UserVo getUserProfile();

    /**
     * 刷新JWT token
     * @param request HTTP请求对象
     * @return 用户登录响应信息
     */
    UserLoginResponse refreshToken(HttpServletRequest request);

}