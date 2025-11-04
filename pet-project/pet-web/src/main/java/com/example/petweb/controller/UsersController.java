package com.example.petweb.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.petcommon.context.UserContext;
import com.example.petcommon.result.Result;
import com.example.petcommon.result.UserLoginResponse;
import com.example.petpojo.dto.ChangePasswordDto;
import com.example.petpojo.dto.UserLoginDto;
import com.example.petpojo.dto.UserUpdateDto;
import com.example.petpojo.dto.UsersCreateDto;
import com.example.petpojo.vo.AdoptionsVo;
import com.example.petpojo.vo.FostersVo;
import com.example.petpojo.vo.UserVo;
import com.example.petservice.service.AdoptionsService;
import com.example.petservice.service.FosterService;
import com.example.petservice.service.UsersService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * 用户管理控制器
 * 提供用户相关的 REST API 接口
 */
@Tag(name = "用户管理", description = "用户相关的所有接口操作")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearer-key")
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
    public Result<UserLoginResponse> createUser(@Parameter(description = "用户创建信息", required = true) @Valid @RequestBody UsersCreateDto userdto) {
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
        @ApiResponse(responseCode = "401", description = "用户名或密码错误"),
        @ApiResponse(responseCode = "400", description = "请求参数错误", 
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.example.petcommon.result.ResultMapStringString.class)
            )
        )
    })
    @PostMapping("/login")
    public Result<UserLoginResponse> login(
            @Parameter(description = "用户登录信息", required = true) @Valid @RequestBody UserLoginDto userLogindto, 
            HttpServletRequest request) {
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
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的信息，包括头像")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "更新失败")
    })
    public Result<UserVo> updateUser(
            @Parameter(description = "用户头像文件") @RequestPart(value = "avatar", required = false) MultipartFile file, 
            @Parameter(description = "用户更新信息") @ModelAttribute UserUpdateDto userUpdateDto) {
        UserVo userVo = usersService.updateUser(userUpdateDto, file);
        return Result.success(userVo);
    }


    /**
     * 更改密码
     */
    @PutMapping("/updatePassword")
    @Operation(summary = "更改密码", description = "修改当前登录用户的密码")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "密码修改成功"),
        @ApiResponse(responseCode = "400", description = "密码修改失败，原密码错误")
    })
    public Result<String> updatePassword(
            @Parameter(description = "密码修改信息", required = true) @RequestBody ChangePasswordDto changePasswordDto) {
        usersService.changePassword(changePasswordDto);
        return Result.success("密码更新成功");
    }

    /**
     * 获取用户领养记录
     */
    @GetMapping("/adoptions")
    @Operation(summary = "获取用户领养记录", description = "分页获取当前用户的领养记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public Result<Map<String, Object>> getUserAdoptions(
            @Parameter(description = "当前页码，从1开始") @RequestParam(value = "current_page", defaultValue = "1") Integer currentPage,
            @Parameter(description = "每页记录数") @RequestParam(value = "per_page", defaultValue = "10") Integer pageSize) {
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
    @Operation(summary = "获取用户寄养记录", description = "分页获取当前用户的寄养记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public Result<Map<String, Object>> getUserFosters(
            @Parameter(description = "当前页码，从1开始") @RequestParam(value = "current_page", defaultValue = "1") Integer currentPage,
            @Parameter(description = "每页记录数") @RequestParam(value = "per_page", defaultValue = "10") Integer pageSize) {
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