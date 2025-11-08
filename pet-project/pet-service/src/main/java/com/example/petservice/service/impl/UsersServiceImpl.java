package com.example.petservice.service.impl;

import com.example.petpojo.dto.ChangePasswordDto;
import com.example.petpojo.dto.UserUpdateDto;
import com.example.petpojo.vo.UserVo;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.petpojo.dto.UsersCreateDto;
import com.example.petpojo.entity.Users;
import com.example.petservice.mapper.UsersMapper;
import com.example.petservice.service.UsersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.petcommon.utils.JwtUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.example.petcommon.result.UserLoginResponse;
import com.example.petcommon.config.JwtProperties;
import com.example.petcommon.context.UserContext;
import com.example.petpojo.dto.UserLoginDto;
import com.example.petpojo.entity.LoginHistory;
import com.example.petpojo.entity.enums.CommonEnum.LoginStatusEnum;
import com.example.petservice.service.LoginHistoryService;
import com.example.petcommon.utils.IpUtil;
import com.example.petcommon.utils.DeviceUtil;
import com.example.petcommon.utils.LocationUtil;
import com.example.petcommon.utils.TokenRefreshUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import io.jsonwebtoken.Claims;


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
    /**
     * 创建用户
     */
    @Override
    public UserLoginResponse createUser(UsersCreateDto userdto) {
        log.info("创建新用户: {}", userdto);
            
        // 检查用户名是否已存在
        Users wq = lambdaQuery().eq(Users::getUserName, userdto.getUserName()).one();
        if(wq != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        
        try {
            Users user = new Users();
            BeanUtils.copyProperties(userdto, user);
            user.setPasswordHash(passwordEncoder.encode(userdto.getPasswordHash()));
            boolean save = this.save(user);
            log.info("用户保存结果: {}", save);
            Long userId = user.getId().longValue();

            // 验证JWT配置
            if (jwtProperties.getUserSecretKey() == null || jwtProperties.getUserTtl() <= 0) {
                log.error("JWT配置异常，secretKey: {}, ttl: {}", jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl());
                throw new IllegalArgumentException("JWT配置异常");
            }
            
            // 生成JWT token
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userId);
            claims.put("username", userdto.getUserName());
            String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
                
            // 构建响应
            UserLoginResponse response = new UserLoginResponse();
            response.setUserId(userId);
            response.setUsername(user.getUserName());
            response.setToken(token);
            response.setExpireTime(System.currentTimeMillis() + jwtProperties.getUserTtl());
            response.setMessage("用户创建成功");
            response.setOk(true); // 添加成功标识
            
            log.info("用户创建成功，用户ID: {}", userId);
            return response;
        } 
        catch (IllegalArgumentException e) {
            // 保留具体的业务异常信息
            log.error("创建用户失败: {}", e.getMessage(), e);
            throw e;
        }
        catch (Exception e) {
            log.error("创建用户失败: {}", e.getMessage(), e);
            throw new IllegalArgumentException("用户创建失败", e);
        }
       
    }
    /**
     * 用户登录
     */
    @Override
    public UserLoginResponse userLogin(UserLoginDto userLoginDto) {
        return userLogin(userLoginDto, null);
    }
    
    /**
     * 用户登录（带请求信息）
     */
    @Override
    public UserLoginResponse userLogin(UserLoginDto userLoginDto, HttpServletRequest request) {
        log.info("用户登录: {}", userLoginDto);
        String username = userLoginDto.getUserName();

        Users user = lambdaQuery().eq(Users::getUserName, username).one();
        if (user == null) {
            // 记录用户名不存在的登录失败历史
            try {
                String ipAddress = request != null ? IpUtil.getIpAddress(request) : "0.0.0.0";
                String userAgent = request != null ? request.getHeader("User-Agent") : "Unknown";
                String device = request != null ? DeviceUtil.getFullDeviceInfo(request) : "Unknown Device";
                String location = LocationUtil.getLocationFromIP(ipAddress);
                
                LoginHistory loginHistory = LoginHistory.builder()
                        .userId(-1) // 使用-1表示未知用户
                        .loginTime(LocalDateTime.now())
                        .ipAddress(ipAddress)
                        .userAgent(userAgent)
                        .device(device)
                        .location(location)
                        .status(LoginStatusEnum.FAILED)
                        .build();
                
                loginHistoryService.save(loginHistory);
                log.info("用户名不存在，登录失败历史记录插入成功，用户名: {}，IP: {}，设备: {}，位置: {}", username, ipAddress, device, location);
            } catch (Exception e) {
                log.error("插入用户名不存在登录失败历史记录失败: {}", e.getMessage(), e);
            }
            throw new IllegalArgumentException("用户名不存在");
        }

        // 验证密码
        if (!passwordEncoder.matches(userLoginDto.getPasswordHash(), user.getPasswordHash())) {
            // 记录登录失败历史
            try {
                String ipAddress = request != null ? IpUtil.getIpAddress(request) : "0.0.0.0";
                String userAgent = request != null ? request.getHeader("User-Agent") : "Unknown";
                String device = request != null ? DeviceUtil.getFullDeviceInfo(request) : "Unknown Device";
                String location = LocationUtil.getLocationFromIP(ipAddress);
                
                LoginHistory loginHistory = LoginHistory.builder()
                         .userId(user.getId().intValue())
                         .loginTime(LocalDateTime.now())
                         .ipAddress(ipAddress)
                         .userAgent(userAgent)
                         .device(device)
                         .location(location)
                         .status(LoginStatusEnum.FAILED)
                         .build();
                
                loginHistoryService.save(loginHistory);
                log.info("登录失败历史记录插入成功，用户ID: {}，IP: {}，设备: {}，位置: {}", user.getId(), ipAddress, device, location);
            } catch (Exception e) {
                log.error("插入登录失败历史记录失败: {}", e.getMessage(), e);
            }
            throw new IllegalArgumentException("密码错误");
        }

        // 验证JWT配置
        if (jwtProperties.getUserSecretKey() == null || jwtProperties.getUserTtl() <= 0) {
            log.error("JWT配置异常，secretKey: {}, ttl: {}", jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl());
            throw new IllegalArgumentException("JWT配置异常");
        }

        // 生成JWT token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().longValue());
        claims.put("username", username);
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        // 构建响应
        UserLoginResponse response = new UserLoginResponse();
        response.setUserId(user.getId().longValue());
        response.setUsername(username);
        response.setToken(token);
        response.setExpireTime(System.currentTimeMillis() + jwtProperties.getUserTtl());
        response.setMessage("用户登录成功");
        response.setOk(true); // 添加成功标识

        log.info("用户登录成功，用户ID: {}", user.getId());
        
        // 插入登录历史记录
        try {
            String ipAddress = request != null ? IpUtil.getIpAddress(request) : "0.0.0.0";
            String userAgent = request != null ? request.getHeader("User-Agent") : "Unknown";
            String device = request != null ? DeviceUtil.getFullDeviceInfo(request) : "Unknown Device";
            String location = LocationUtil.getLocationFromIP(ipAddress);
            
            LoginHistory loginHistory = LoginHistory.builder()
                    .userId(user.getId().intValue())
                    .loginTime(LocalDateTime.now())
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .device(device)
                    .location(location)
                    .status(LoginStatusEnum.SUCCESS)
                    .build();
            
            loginHistoryService.save(loginHistory);
            log.info("登录历史记录插入成功，用户ID: {}，IP: {}，设备: {}，位置: {}", user.getId(), ipAddress, device, location);
            log.debug("【调试】登录记录详情 - userId: {}, ipAddress: {}, device: {}, location: {}, userAgent: {}", 
                    user.getId(), ipAddress, device, location, userAgent);
        } catch (Exception e) {
            log.error("插入登录历史记录失败: {}", e.getMessage(), e);
            // 不抛出异常，避免影响登录流程
        }
        
        return response;
    }

    /**
     * 更新用户信息
     */
    @Override
    public UserVo updateUser(@ModelAttribute  UserUpdateDto userUpdateDto, @RequestPart MultipartFile file) {
        log.info("更新用户信息: {}", userUpdateDto);
        Long userId = UserContext.getCurrentUserId();
        Users user = lambdaQuery().eq(Users::getId, userId).one();
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        
        // 检查用户名唯一性（如果用户名发生变更且不为空）
        if (userUpdateDto.getUserName() != null && !userUpdateDto.getUserName().isEmpty() && !userUpdateDto.getUserName().equals(user.getUserName())) {
            Users existingUser = lambdaQuery().eq(Users::getUserName, userUpdateDto.getUserName()).ne(Users::getId, userId).one();
            if (existingUser != null) {
                throw new IllegalArgumentException("该用户名已被其他用户使用");
            }
            user.setUserName(userUpdateDto.getUserName());
        }
        
        // 检查邮箱唯一性（如果邮箱发生变更且不为空）
        if (userUpdateDto.getEmail() != null && !userUpdateDto.getEmail().isEmpty() && !userUpdateDto.getEmail().equals(user.getEmail())) {
            Users existingUser = lambdaQuery().eq(Users::getEmail, userUpdateDto.getEmail()).ne(Users::getId, userId).one();
            if (existingUser != null) {
                throw new IllegalArgumentException("该邮箱已被其他用户使用");
            }
            user.setEmail(userUpdateDto.getEmail());
        }
        
        // 只更新发生变更且不为空的字段
        if (userUpdateDto.getPhone() != null && !userUpdateDto.getPhone().isEmpty() && !userUpdateDto.getPhone().equals(user.getPhone())) {
            user.setPhone(userUpdateDto.getPhone());
        }
        
        if (userUpdateDto.getIntroduce() != null && !userUpdateDto.getIntroduce().equals(user.getIntroduce())) {
            user.setIntroduce(userUpdateDto.getIntroduce());
        }
        
        if (file != null && !file.isEmpty()) {
            try {
                String name = file.getOriginalFilename();
                String fileExtension = null;
                if (name != null) {
                    fileExtension = name.substring(name.lastIndexOf("."));
                }
                String fileName= UUID.randomUUID().toString() + fileExtension;
                log.info("开始上传头像文件，原始文件名: {}, 新文件名: {}", name, fileName);

                // 保存到项目源目录的静态资源目录，打包后会自动包含
                String projectPath = System.getProperty("user.dir");
                String filePath = projectPath + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "static" + File.separator + "images" + File.separator + fileName;
                log.info("文件保存路径: {}", filePath);
                
                File parentDir = new File(filePath).getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    boolean created = parentDir.mkdirs();
                    log.info("创建目录: {}, 结果: {}", parentDir.getAbsolutePath(), created);
                }
                
                file.transferTo(new File(filePath));
                log.info("文件上传成功，文件大小: {} bytes", file.getSize());
                
                // 确保文件完全写入磁盘
                File savedFile = new File(filePath);
                if (!savedFile.exists() || savedFile.length() == 0) {
                    log.error("文件保存验证失败，文件不存在或大小为0");
                    throw new IllegalArgumentException("文件保存失败");
                }
                log.info("文件保存验证成功，文件路径: {}, 大小: {} bytes", savedFile.getAbsolutePath(), savedFile.length());
                
                user.setHeadPic(fileName);
                log.info("设置用户头像字段: {}", fileName);
            } catch (IOException e) {
                log.error("文件上传失败: {}", e.getMessage(), e);
                throw new IllegalArgumentException("文件上传失败", e);
            }
        } else {
            log.info("没有上传新头像文件，file为null或为空");
        }
        boolean updated = this.updateById(user);
        if (!updated) {
            throw new IllegalArgumentException("用户更新失败");
        }
        
        log.info("用户信息更新成功，userId: {}, 头像文件名: {}", user.getId(), user.getHeadPic());
        
        UserVo userVo = new UserVo();
        // 手动映射字段，因为字段名不完全匹配
        userVo.setUserId(user.getId() != null ? user.getId().longValue() : null);
        userVo.setUserName(user.getUserName());
        userVo.setPhone(user.getPhone());
        userVo.setIntroduce(user.getIntroduce());
        userVo.setHeadPic(user.getHeadPic());
        userVo.setEmail(user.getEmail());
        
        log.info("返回UserVo: userId={}, userName={}", userVo.getUserId(), userVo.getUserName());

        return userVo;
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
        
        if (userId == null) {
            log.error("无法从UserContext获取用户ID");
            throw new IllegalArgumentException("无法获取当前用户ID");
        }
        
        // 直接使用Long类型查询，避免类型转换问题
        Users user = lambdaQuery().eq(Users::getId, userId).one();
        if (user == null) {
            log.warn("用户不存在，userId: {}", userId);
            throw new IllegalArgumentException("用户不存在");
        }
        log.info("查询到的用户: id={}, userName={}", user.getId(), user.getUserName());
        UserVo userVo = new UserVo();
        // 手动映射字段，因为字段名不完全匹配
        userVo.setUserId(user.getId() != null ? user.getId().longValue() : null);
        userVo.setUserName(user.getUserName());
        userVo.setPhone(user.getPhone());
        userVo.setIntroduce(user.getIntroduce());
        userVo.setHeadPic(user.getHeadPic());
        userVo.setEmail(user.getEmail());
        log.info("返回的UserVo: userId={}, userName={}", userVo.getUserId(), userVo.getUserName());
        return userVo;
    }

    /**
     * 刷新JWT token
     * 使用当前有效或过期的token来获取新token
     */
    @Override
    public UserLoginResponse refreshToken(HttpServletRequest request) {
        log.info("用户刷新token");
        
        // 从请求头获取当前token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            token = request.getHeader("authentication");
        }
        
        if (token == null || token.isEmpty()) {
            log.error("无法从请求头获取token");
            throw new IllegalArgumentException("请先登录");
        }
        
        // 移除Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        try {
            // 使用宽松模式解析token（允许过期）
            Claims claims = JwtUtil.parseJWTLoose(jwtProperties.getUserSecretKey(), token);
            
            // 检查token是否已过期
            if (JwtUtil.isTokenExpired(claims)) {
                log.warn("token已过期，但仍允许刷新");
            }
            
            // 从claims中提取userId和username
            Long userId = TokenRefreshUtil.extractUserId(claims);
            String username = TokenRefreshUtil.extractUsername(claims);
            
            if (userId == null) {
                log.error("无法从token中提取userId");
                throw new IllegalArgumentException("token无效或已过期");
            }
            
            if (username == null) {
                log.warn("无法从token中提取username，使用默认值");
                username = "";
            }
            
            log.info("token刷新成功，userId: {}, username: {}", userId, username);
            
            // 生成新的token
            Map<String, Object> newClaims = new HashMap<>();
            newClaims.put("userId", userId);
            newClaims.put("username", username);
            String newToken = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), newClaims);
            
            // 构建响应
            UserLoginResponse response = new UserLoginResponse();
            response.setUserId(userId);
            response.setUsername(username);
            response.setToken(newToken);
            response.setExpireTime(System.currentTimeMillis() + jwtProperties.getUserTtl());
            response.setMessage("token刷新成功");
            response.setOk(true);
            
            return response;
        } catch (Exception e) {
            log.error("token刷新失败: {}", e.getMessage(), e);
            throw new IllegalArgumentException("token刷新失败: " + e.getMessage());
        }
    }

}