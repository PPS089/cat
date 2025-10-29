package com.example.petweb.controller;

import java.util.Map;

import com.example.petpojo.dto.ChangePasswordDto;
import com.example.petpojo.dto.UserUpdateDto;
import com.example.petcommon.utils.UserContext;
import com.example.petpojo.vo.UserVo;
import com.example.petpojo.vo.AdoptionsVo;
import com.example.petservice.service.AdoptionsService;
import com.example.petpojo.vo.FostersVo;
import com.example.petservice.service.FosterService;
import org.springframework.web.bind.annotation.*;

import com.example.petcommon.result.Result;
import com.example.petcommon.result.UserLoginResponse;
import com.example.petpojo.dto.UserLoginDto;
import com.example.petpojo.dto.UsersCreateDto;
import com.example.petservice.service.UsersService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;


/**
 * 用户管理控制器
 * 提供用户相关的 REST API 接口
 */
@Tag(name = "用户管理", description = "用户相关的所有接口操作")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UsersController {

    private final UsersService usersService;
    private final AdoptionsService adoptionsService;
    private final FosterService fosterService;

    /**
     * 创建新用户
     */
    @Operation(summary = "创建新用户", description = "注册新用户账号")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "用户创建成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    @PostMapping
    public Result<UserLoginResponse> createUser(@Valid @RequestBody UsersCreateDto userdto) {
        log.info("创建新用户: {}", userdto);
        UserLoginResponse response = usersService.createUser(userdto);
        
        return Result.success(response);
    }

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "用户使用用户名和密码登录系统")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "登录成功"),
        @ApiResponse(responseCode = "401", description = "用户名或密码错误")
    })
    @PostMapping("/login")
    public Result<UserLoginResponse> login(@Valid @RequestBody UserLoginDto userLogindto, HttpServletRequest request) {
        log.info("用户登录: {}", userLogindto);
        UserLoginResponse response = usersService.userLogin(userLogindto, request);


        return Result.success(response);
    }


    /**
     * 获取用户信息
     */
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    @ApiResponse(responseCode = "200", description = "获取用户信息成功")
    @GetMapping("/profile")
    public Result<UserVo> getUserProfile() {
        // 添加调试信息
        Long currentUserId = UserContext.getCurrentUserId();
        String currentUserName = UserContext.getCurrentUserName();
        log.info("获取用户信息 - 当前用户上下文: userId={}, username={}", currentUserId, currentUserName);
        
        UserVo userVo = usersService.getUserProfile();
        log.info("返回用户信息: userId={}, username={}", userVo.getUserId(), userVo.getUserName());
        return Result.success(userVo);
    }



    /**
     * 更新用户信息
     */
    @PutMapping("/profile")
    public Result<UserVo> updateUser(@RequestPart(value = "avatar", required = false) MultipartFile file, @ModelAttribute UserUpdateDto userUpdateDto) {
        UserVo userVo = usersService.updateUser(userUpdateDto, file);
        return Result.success(userVo);
    }


    /**
     * 更改密码
     */
    @PutMapping("/updatePassword")
    public Result<String> updatePassword(@RequestBody ChangePasswordDto changePasswordDto) {
        usersService.changePassword(changePasswordDto);
        return Result.success("密码更新成功");
    }

    /**
     * 获取用户领养记录
     */
    @GetMapping("/adoptions")
    public Result<Map<String, Object>> getUserAdoptions(
            @RequestParam(value = "current_page", defaultValue = "1") Integer currentPage,
            @RequestParam(value = "per_page", defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getCurrentUserId();
        log.info("获取用户领养记录: userId={}, currentPage={}, pageSize={}", userId, currentPage, pageSize);
        // 如果pageSize大于100，限制为100条记录
        if (pageSize > 100) {
            pageSize = 100;
        }
        // 使用新的分页方法，返回总记录数
        com.baomidou.mybatisplus.core.metadata.IPage<AdoptionsVo> pageResult = 
            adoptionsService.getUserAdoptionsWithPage(userId, currentPage, pageSize);
        
        // 返回MyBatis-Plus标准分页格式 - 匹配前端期望的格式
        return Result.success(Map.of(
            "records", pageResult.getRecords(),
            "total", pageResult.getTotal(),
            "size", pageResult.getSize(),
            "current", pageResult.getCurrent(),
            "pages", pageResult.getPages()
        ));
    }

    /**
     * 获取用户寄养记录
     */
    @GetMapping("/fosters")
    public Result<Map<String, Object>> getUserFosters(
            @RequestParam(value = "current_page", defaultValue = "1") Integer currentPage,
            @RequestParam(value = "per_page", defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getCurrentUserId();
        // 如果pageSize大于100，限制为100条记录
        if (pageSize > 100) {
            pageSize = 100;
        }
        // 使用新的分页方法，返回MyBatis-Plus IPage对象
        com.baomidou.mybatisplus.core.metadata.IPage<FostersVo> pageResult = 
            fosterService.getUserFostersWithPage(userId, currentPage, pageSize);
        
        // 返回MyBatis-Plus标准分页格式
        return Result.success(Map.of(
            "records", pageResult.getRecords(),
            "total", pageResult.getTotal(),
            "size", pageResult.getSize(),
            "current", pageResult.getCurrent(),
            "pages", pageResult.getPages()
        ));
    }

    /**
     * 刷新JWT token
     */
    @PostMapping("/refresh-token")
    public Result<UserLoginResponse> refreshToken(HttpServletRequest request) {
        log.info("用户刷新token");
        UserLoginResponse response = usersService.refreshToken(request);
        return Result.success(response);
    }

}