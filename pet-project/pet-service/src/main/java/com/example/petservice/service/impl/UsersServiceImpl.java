package com.example.petservice.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petcommon.context.UserContext;
import com.example.petcommon.error.ErrorCode;
import com.example.petcommon.exception.BizException;
import com.example.petcommon.properties.JwtProperties;
import com.example.petcommon.utils.DeviceUtil;
import com.example.petcommon.utils.IpLocationUtil;
import com.example.petcommon.utils.IpUtil;
import com.example.petcommon.utils.JwtUtil;
import com.example.petpojo.dto.ChangePasswordDto;
import com.example.petpojo.dto.UserLoginDto;
import com.example.petpojo.dto.UserUpdateDto;
import com.example.petpojo.dto.UsersCreateDto;
import com.example.petpojo.entity.LoginHistory;
import com.example.petpojo.entity.Users;
import com.example.petpojo.entity.enums.CommonEnum.LoginStatusEnum;
import com.example.petpojo.vo.UserLoginVo;
import com.example.petpojo.vo.UserVo;
import com.example.petservice.config.AliyunOSSOperator;
import com.example.petservice.mapper.UsersMapper;
import com.example.petservice.service.LoginHistoryService;
import com.example.petservice.service.UsersService;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户服务实现类
 * 实现用户相关的业务逻辑，包括用户注册、登录、信息更新等
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements UsersService {
   
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;
    private final LoginHistoryService loginHistoryService;

    @Autowired
    private AliyunOSSOperator ossOperator;

    /**
     * 创建用户
     */
    @Override
    public UserLoginVo createUser(UsersCreateDto userdto) {
        log.info("创建新用户: {}", userdto.getUserName());
            
        // 检查用户名是否已存在
        if (lambdaQuery().eq(Users::getUserName, userdto.getUserName()).exists()) {
            throw new BizException(ErrorCode.USERNAME_EXISTS);
        }
        
        try {
            // 创建用户
            Users user = new Users();
            BeanUtils.copyProperties(userdto, user);
            user.setPasswordHash(passwordEncoder.encode(userdto.getPasswordHash()));
            this.save(user);
            
            log.info("用户创建成功，用户ID: {}", user.getId());
            
            // 生成token并返回响应
            return buildLoginResponse(user.getId().longValue(), user.getUserName());
        } 
        catch (IllegalArgumentException e) {
            log.error("创建用户失败: {}", e.getMessage(), e);
            throw e;
        }
        catch (Exception e) {
            log.error("创建用户失败: {}", e.getMessage(), e);
            throw new BizException(ErrorCode.INTERNAL_ERROR, "用户创建失败");
        }
    }
   
    /**
     * 用户登录（带请求信息）
     */
    @Override
    public UserLoginVo userLogin(UserLoginDto userLoginDto, HttpServletRequest request) {
        log.info("用户登录: {}", userLoginDto.getUserName());
        String username = userLoginDto.getUserName();

        Users user = lambdaQuery().eq(Users::getUserName, username).one();
        if (user == null) {
            log.info("用户名不存在: {}", username);
            
            // 返回登录失败的响应
            UserLoginVo response = new UserLoginVo();
            response.setUserId(null);
            response.setUsername(username);
            response.setAccessToken(null);
            response.setExpireTime(0L);
            response.setMessage("用户名或密码错误");
            response.setOk(false);
            
            return response;
        }

        // 验证密码
        if (!passwordEncoder.matches(userLoginDto.getPasswordHash(), user.getPasswordHash())) {
            // 记录登录失败历史
            recordLoginHistory(user.getId(), username, request, LoginStatusEnum.FAILED);
            
            // 返回登录失败的响应
            UserLoginVo response = new UserLoginVo();
            response.setUserId(user.getId().longValue());
            response.setUsername(username);
            response.setAccessToken(null);
            response.setExpireTime(0L);
            response.setMessage("用户名或密码错误");
            response.setOk(false);
            
            return response;
        }

        // 验证JWT配置
        if (jwtProperties.getUserSecretKey() == null || jwtProperties.getUserTtl() <= 0) {
            log.error("JWT配置异常，secretKey: {}, ttl: {}", jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl());
            throw new BizException(ErrorCode.JWT_CONFIG_ERROR);
        }

        UserLoginVo response = buildLoginResponse(user.getId().longValue(), username);

        log.info("用户登录成功，用户ID: {}", user.getId());
        recordLoginHistory(user.getId(), username, request, LoginStatusEnum.SUCCESS);
        return response;
    }

    /**
     * 更新用户信息
     */
    @Override
    public UserVo updateUser(UserUpdateDto userUpdateDto, MultipartFile file) {
        log.info("更新用户信息: {}", userUpdateDto);
        Long userId = UserContext.getCurrentUserId();
        Users user = lambdaQuery().eq(Users::getId, userId).one();
        if (user == null) {
            throw new BizException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 检查用户名唯一性
        checkFieldUniqueness("userName", userUpdateDto.getUserName(), userId, ErrorCode.USERNAME_EXISTS, "用户名已存在");
        
        // 检查邮箱唯一性
        checkFieldUniqueness("email", userUpdateDto.getEmail(), userId, ErrorCode.BAD_REQUEST, "该邮箱已被其他用户使用");
        
        // 更新用户信息
        updateFieldIfChanged(userUpdateDto.getUserName(), user.getUserName(), user::setUserName);
        updateFieldIfChanged(userUpdateDto.getEmail(), user.getEmail(), user::setEmail);
        updateFieldIfChanged(userUpdateDto.getPhone(), user.getPhone(), user::setPhone);
        updateFieldIfChanged(userUpdateDto.getIntroduce(), user.getIntroduce(), user::setIntroduce);
        
        // 处理头像上传（如果有文件）
        if (file != null && !file.isEmpty()) {
            try {
                String name = file.getOriginalFilename();
                String oldHeadPic = user.getHeadPic();
                
                // 上传新文件到OSS
                String fileUrl = ossOperator.upload(file.getBytes(), name);
                user.setHeadPic(fileUrl);
                
                // 删除原头像文件（异步处理，不影响主流程）
                deleteOldHeadPic(oldHeadPic);
            } catch (Exception e) {
                log.error("头像上传失败: {}", e.getMessage(), e);
                throw new BizException(ErrorCode.FILE_UPLOAD_FAILED);
            }
        }
        
        // 更新用户信息
        boolean updated = this.updateById(user);
        if (!updated) {
            throw new BizException(ErrorCode.USER_UPDATE_FAILED);
        }
        
        log.info("用户信息更新成功，userId: {}", user.getId());
        
        // 使用辅助方法构建UserVo
        return buildUserVo(user);
    }

    /**
     * 更改密码
     */
    @Override
    public  boolean changePassword(ChangePasswordDto changePasswordDto) {
        log.info("修改用户密码: {}", changePasswordDto);
        Long userId = UserContext.getCurrentUserId();
        Users user = lambdaQuery().eq(Users::getId, userId).one();
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        // 验证旧密码
        if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("旧密码错误");
        }
        // 更新密码
        user.setPasswordHash(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        boolean updated = this.updateById(user);
        if (!updated) {
            throw new IllegalArgumentException("密码更新失败");
        }
        return true;
    }
    /**
     * 获取用户个人信息
     */
    @Override
    public UserVo getUserProfile() {
        log.info("获取用户个人信息");
        Long userId = UserContext.getCurrentUserId();
        log.info("从UserContext获取到的userId: {}", userId);
        
        Users user = lambdaQuery().eq(Users::getId, userId).one();
        if (user == null) {
            log.warn("用户不存在，userId: {}", userId);
            throw new BizException(ErrorCode.USER_NOT_FOUND);
        }
        
        log.info("查询到的用户: id={}, userName={}", user.getId(), user.getUserName());
        
        // 使用辅助方法构建UserVo
        UserVo userVo = buildUserVo(user);
        log.info("返回的UserVo: userId={}, userName={}", userVo.getUserId(), userVo.getUserName());
        
        return userVo;
    }

    /**
     * 刷新JWT token
     * 使用当前有效或过期的token来获取新token
     */
    @Override
    public UserLoginVo refreshToken(HttpServletRequest request) {
        log.info("用户刷新token");
        
        String token = request.getHeader("Authorization");
        
        if (token == null || token.isEmpty()) {
            log.error("无法从请求头获取token");
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // 验证JWT配置
        if (jwtProperties.getUserSecretKey() == null || jwtProperties.getUserTtl() <= 0 || 
            jwtProperties.getRefreshSecretKey() == null || jwtProperties.getRefreshTtl() <= 0) {
            log.error("JWT配置异常，userSecretKey: {}, userTtl: {}, refreshSecretKey: {}, refreshTtl: {}", 
                     jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), 
                     jwtProperties.getRefreshSecretKey(), jwtProperties.getRefreshTtl());
            throw new BizException(ErrorCode.JWT_CONFIG_ERROR);
        }
        
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getRefreshSecretKey(), token);
            
            // 从claims中提取userId和username
            Long userId = JwtUtil.extractUserId(claims);
            String username = JwtUtil.extractUsername(claims);
            
            if (userId == null) {
                log.error("无法从token中提取userId");
                throw new BizException(ErrorCode.TOKEN_INVALID_OR_EXPIRED);
            }
            
            // 使用默认值处理username为null的情况
            username = username != null ? username : "";

            Map<String, Object> newClaims = new HashMap<>();
            newClaims.put("userId", userId);
            newClaims.put("username", username);
            String newAccessToken = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), newClaims);
            String newRefreshToken = JwtUtil.createJWT(jwtProperties.getRefreshSecretKey(), jwtProperties.getRefreshTtl(), newClaims);

            UserLoginVo response = new UserLoginVo();
            response.setUserId(userId);
            response.setUsername(username);
            response.setAccessToken(newAccessToken);
            response.setRefreshToken(newRefreshToken);
            response.setExpireTime(System.currentTimeMillis() + jwtProperties.getUserTtl());
            response.setMessage("token刷新成功");
            response.setOk(true);
            return response;
        } catch (Exception e) {
            log.error("token刷新失败: {}", e.getMessage(), e);
            if (e instanceof IllegalArgumentException) {
                throw new BizException(ErrorCode.BAD_REQUEST, "token刷新失败: " + e.getMessage());
            } else {
                throw new BizException(ErrorCode.INTERNAL_ERROR, "token刷新失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 构建登录响应
     */
    private UserLoginVo buildLoginResponse(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        String accessToken = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        String refreshToken = JwtUtil.createJWT(jwtProperties.getRefreshSecretKey(), jwtProperties.getRefreshTtl(), claims);
        UserLoginVo response = new UserLoginVo();
        response.setUserId(userId);
        response.setUsername(username);
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpireTime(System.currentTimeMillis() + jwtProperties.getUserTtl());
        response.setMessage("操作成功");
        response.setOk(true);
        return response;
    }
    
    /**
     * 记录登录历史
     */
    private void recordLoginHistory(Integer userId, String username, HttpServletRequest request, LoginStatusEnum status) {
        try {
            String ipAddress = request != null ? IpUtil.getIpAddress(request) : "0.0.0.0";
            String userAgent = request != null ? request.getHeader("User-Agent") : "Unknown";
            String device = request != null ? DeviceUtil.getFullDeviceInfo(request) : "Unknown Device";
            String location = IpLocationUtil.getLocationFromIP(ipAddress);
            
            LoginHistory loginHistory = LoginHistory.builder()
                    .userId(userId != null ? userId : -1) // 使用-1表示未知用户
                    .loginTime(LocalDateTime.now())
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .device(device)
                    .location(location)
                    .status(status)
                    .build();
            
            loginHistoryService.save(loginHistory);
            
            if (status == LoginStatusEnum.SUCCESS) {
                log.info("登录历史记录插入成功，用户ID: {}，IP: {}，设备: {}，位置: {}", userId, ipAddress, device, location);
                log.debug("【调试】登录记录详情 - userId: {}, ipAddress: {}, device: {}, location: {}, userAgent: {}", 
                        userId, ipAddress, device, location, userAgent);
            } else {
                log.info("登录失败历史记录插入成功，用户名: {}，IP: {}，设备: {}，位置: {}", username, ipAddress, device, location);
            }
        } catch (Exception e) {
            log.error("插入登录历史记录失败: {}", e.getMessage(), e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 检查字段唯一性
     * @param field 字段名
     * @param value 字段值
     * @param userId 当前用户ID
     * @param errorCode 错误码
     * @param errorMessage 错误信息
     */
    private void checkFieldUniqueness(String field, String value, Long userId, ErrorCode errorCode, String errorMessage) {
        if (value == null || value.isEmpty()) {
            return;
        }
        
        Users existingUser = switch (field) {
            case "userName" -> lambdaQuery().eq(Users::getUserName, value).ne(Users::getId, userId).one();
            case "email" -> lambdaQuery().eq(Users::getEmail, value).ne(Users::getId, userId).one();
            default -> null;
        };
        
        if (existingUser != null) {
            throw new BizException(errorCode, errorMessage);
        }
    }
    
    /**
     * 更新用户字段（如果值发生变化且不为空）
     * @param newValue 新值
     * @param currentValue 当前值
     * @param updater 更新方法
     */
    private <T> void updateFieldIfChanged(T newValue, T currentValue, Consumer<T> updater) {
        if (newValue != null && !newValue.equals(currentValue)) {
            updater.accept(newValue);
        }
    }
    
    /**
     * 构建UserVo对象
     * @param user 用户实体
     * @return UserVo对象
     */
    private UserVo buildUserVo(Users user) {
        UserVo userVo = new UserVo();
        userVo.setUserId(user.getId() != null ? user.getId().longValue() : null);
        userVo.setUserName(user.getUserName());
        userVo.setPhone(user.getPhone());
        userVo.setIntroduce(user.getIntroduce());
        userVo.setEmail(user.getEmail());
        
        // 处理头像URL
        String headPic = user.getHeadPic();
        if (headPic != null && !headPic.isEmpty()) {
            userVo.setHeadPic(headPic.startsWith("http") ? headPic : "/images/" + headPic);
        } else {
            userVo.setHeadPic(null);
        }
        
        return userVo;
    }
    
    /**
     * 删除旧头像文件
     * @param oldHeadPic 旧头像URL
     */
    private void deleteOldHeadPic(String oldHeadPic) {
        if (oldHeadPic != null && !oldHeadPic.isEmpty()) {
            try {
                // 只处理OSS文件删除
                if (oldHeadPic.startsWith("https://") || oldHeadPic.startsWith("http://")) {
                    ossOperator.delete(oldHeadPic);
                }
                // 对于非URL格式的头像（可能是本地文件），不做处理
            } catch (Exception e) {
                // 不中断主流程，继续执行
                log.warn("删除原头像文件失败: {}", e.getMessage());
            }
        }
    }

}
