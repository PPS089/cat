package com.example.petweb.controller;

import java.util.Map;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.petcommon.context.UserContext;
import com.example.petcommon.result.Result;
import com.example.petpojo.vo.UserLoginVo;
import com.example.petpojo.dto.ChangePasswordDto;
import com.example.petpojo.dto.ForgotPasswordDto;
import com.example.petpojo.dto.RefreshTokenRequest;
import com.example.petpojo.dto.SendResetCodeDto;
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
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * 用户管理控制器
 * 提供用户相关的 REST API 接口
 * @author 33185
 */
@Tag(name = "用户管理", description = "用户相关的所有接口操作")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearer-key")
@Validated
public class UsersController {

    private final UsersService usersService;
    private final AdoptionsService adoptionsService;
    private final FosterService fosterService;

    /**
     * 创建新用户
     */
    @Operation(summary = "创建新用户", description = "注册新用户账号")
    @PostMapping("/register")
    public Result<UserLoginVo> createUser(@Valid @RequestBody UsersCreateDto userdto) {
        log.info("创建新用户: {}", userdto);
        UserLoginVo response = usersService.createUser(userdto);
        return Result.success(response);
    }

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "用户使用用户名和密码登录系统")
    @PostMapping("/login")
    public Result<UserLoginVo> login(
            @Valid @RequestBody UserLoginDto userLogindto, 
            HttpServletRequest request) {
        log.info("用户登录: {}", userLogindto);
        UserLoginVo response = usersService.userLogin(userLogindto, request);
        return Result.success(response);
    }


    /**
     * 获取用户信息
     */
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/profile")
    public Result<UserVo> getUserProfile() {
        Long currentUserId = UserContext.getCurrentUserId();
        String currentUserName = UserContext.getCurrentUserName();
        log.info("获取用户信息 - 当前用户上下文: userId={}, username={}", currentUserId, currentUserName);
        UserVo userVo = usersService.getUserProfile();
        return Result.success(userVo);
    }



    /**
     * 更新用户信息
     */
    @PutMapping(value = "/profile", consumes = "multipart/form-data")
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的信息，包括头像")
    public Result<UserVo> updateUser(
            @RequestParam(value = "avatar", required = false) MultipartFile file, 
            @RequestParam(value = "userName", required = false) @Size(min = 1, max = 10, message = "用户名长度需在1-10字符内") String userName,
            @RequestParam(value = "phone", required = false) @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String phone,
            @RequestParam(value = "introduce", required = false) @Size(max = 200, message = "个人简介长度不能超过200字符") String introduce,
            @RequestParam(value = "email", required = false) @Email(message = "邮箱格式不正确") String email) {
        // 构建UserUpdateDto对象
        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setUserName(userName);
        userUpdateDto.setPhone(phone);
        userUpdateDto.setIntroduce(introduce);
        userUpdateDto.setEmail(email);
        UserVo userVo = usersService.updateUser(userUpdateDto, file);
        return Result.success(userVo);
    }


    /**
     * 更改密码
     */
    @PutMapping({"/updatePassword", "/update-password"})
    @Operation(summary = "更改密码", description = "修改当前登录用户的密码")
    public Result<String> updatePassword(
            @Valid @RequestBody ChangePasswordDto changePasswordDto) {
        usersService.changePassword(changePasswordDto);
        return Result.success("密码更新成功");
    }

    /**
     * 获取用户领养记录
     */
    @GetMapping("/adoptions")
    @Operation(summary = "获取用户领养记录", description = "分页获取当前用户的领养记录")
    public Result<Map<String, Object>> getUserAdoptions(
            @RequestParam(value = "current_page", defaultValue = "1") @Min(value = 1, message = "current_page 必须>=1") Integer currentPage,
            @RequestParam(value = "per_page", defaultValue = "10") @Min(value = 1, message = "per_page 必须>=1") Integer pageSize,
            @RequestParam(value = "status", required = false) @Size(max = 20, message = "状态长度不能超过20字符") String status) {
        Long userId = UserContext.getCurrentUserId();
        log.info("获取用户领养记录: userId={}, currentPage={}, pageSize={}", userId, currentPage, pageSize);
        if (pageSize > 100) {
            pageSize = 100;
        }
        IPage<AdoptionsVo> pageResult = adoptionsService.getUserAdoptionsWithPage(userId, currentPage, pageSize, status);
        return Result.success(Map.of(
            "records", pageResult.getRecords(),
            "total", pageResult.getTotal(),
            "size", pageResult.getSize(),
            "current", pageResult.getCurrent(),
            "pages", pageResult.getPages()
        ));
    }

    /**
     * 获取用户已领养成功的宠物（仅 APPROVED，用于“我的宠物”）
     */
    @GetMapping("/adoptions/approved")
    @Operation(summary = "获取用户已领养宠物", description = "分页获取当前用户已通过(approved)的领养记录，用于“我的宠物”展示")
    public Result<Map<String, Object>> getUserApprovedAdoptions(
            @RequestParam(value = "current_page", defaultValue = "1") @Min(value = 1, message = "current_page 必须>=1") Integer currentPage,
            @RequestParam(value = "per_page", defaultValue = "10") @Min(value = 1, message = "per_page 必须>=1") Integer pageSize) {
        Long userId = UserContext.getCurrentUserId();
        log.info("获取用户已通过领养记录: userId={}, currentPage={}, pageSize={}", userId, currentPage, pageSize);
        if (pageSize > 100) {
            pageSize = 100;
        }
        IPage<AdoptionsVo> pageResult = adoptionsService.getUserApprovedAdoptionsWithPage(userId, currentPage, pageSize);
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
    public Result<Map<String, Object>> getUserFosters(
            @RequestParam(value = "current_page", defaultValue = "1") @Min(value = 1, message = "current_page 必须>=1") Integer currentPage,
            @RequestParam(value = "per_page", defaultValue = "10") @Min(value = 1, message = "per_page 必须>=1") Integer pageSize,
            @RequestParam(value = "status", required = false) @Size(max = 20, message = "状态长度不能超过20字符") String status) {
        Long userId = UserContext.getCurrentUserId();
        if (pageSize > 100) {
            pageSize = 100;
        }
        IPage<FostersVo> pageResult =
            fosterService.getUserFostersWithPage(userId, currentPage, pageSize, status);
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
    @Operation(summary = "刷新JWT token", description = "通过refreshToken获取新的accessToken")
    public Result<UserLoginVo> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        log.info("用户刷新token");
        UserLoginVo response = usersService.refreshToken(refreshTokenRequest.getRefreshToken());
        return Result.success(response);
    }

    /**
     * 发送重置密码验证码
     */
    @Operation(summary = "发送重置密码验证码", description = "向用户邮箱发送重置密码验证码")
    @PostMapping("/send-reset-code")
    public Result<String> sendResetCode(@Valid @RequestBody SendResetCodeDto dto) {
        log.info("发送重置密码验证码请求，email: {}", dto.email());
        usersService.sendResetCode(dto.email());
        return Result.success("验证码已发送到您的邮箱");
    }

    /**
     * 重置密码
     */
    @Operation(summary = "重置密码", description = "使用验证码重置用户密码")
    @PostMapping("/forgot-password")
    public Result<String> forgotPassword(@Valid @RequestBody ForgotPasswordDto dto) {
        log.info("重置密码请求，email: {}", dto.email());
        usersService.resetPassword(dto.email(), dto.code(), dto.newPassword());
        return Result.success("密码重置成功");
    }

}
