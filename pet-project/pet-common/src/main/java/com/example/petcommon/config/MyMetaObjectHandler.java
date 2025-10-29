package com.example.petcommon.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.example.petcommon.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 * 使用MyBatis-Plus官方推荐的自动填充方式
 * 支持多种字段名格式，提高兼容性
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始插入填充...");
        
        LocalDateTime now = LocalDateTime.now();
        Long currentUserId = UserContext.getCurrentUserId();
        String currentUserName = UserContext.getCurrentUserName();
        
        // 填充创建时间（支持多种字段名格式）
        fillCreateTime(metaObject, now);
        
        // 填充更新时间（支持多种字段名格式）
        fillUpdateTime(metaObject, now);
        
        // 填充创建人信息（支持多种字段名格式）
        fillCreateUser(metaObject, currentUserId, currentUserName);
        
        // 填充更新人信息（支持多种字段名格式）
        fillUpdateUser(metaObject, currentUserId, currentUserName);
        
        // 填充开始日期（针对fosters表的start_date字段）
        fillStartDate(metaObject, now);
        
        log.debug("插入填充完成");
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始更新填充...");
        
        LocalDateTime now = LocalDateTime.now();
        Long currentUserId = UserContext.getCurrentUserId();
        String currentUserName = UserContext.getCurrentUserName();
        
        // 填充更新时间（支持多种字段名格式）
        fillUpdateTime(metaObject, now);
        
        // 填充更新人信息（支持多种字段名格式）
        fillUpdateUser(metaObject, currentUserId, currentUserName);
        
        log.debug("更新填充完成");
    }
    
    /**
     * 填充创建时间（支持多种字段名格式）
     */
    private void fillCreateTime(MetaObject metaObject, LocalDateTime now) {
        String[] createTimeFields = {"createTime", "createAt", "createdTime", "createdAt", "gmtCreate", "created_at"};
        for (String fieldName : createTimeFields) {
            if (metaObject.hasSetter(fieldName)) {
                this.strictInsertFill(metaObject, fieldName, LocalDateTime.class, now);
                break;
            }
        }
    }
    
    /**
     * 填充更新时间（支持多种字段名格式）
     */
    private void fillUpdateTime(MetaObject metaObject, LocalDateTime now) {
        String[] updateTimeFields = {"updateTime", "updateAt", "updatedTime", "updatedAt", "gmtModified", "updated_at"};
        for (String fieldName : updateTimeFields) {
            if (metaObject.hasSetter(fieldName)) {
                this.strictInsertFill(metaObject, fieldName, LocalDateTime.class, now);
                this.strictUpdateFill(metaObject, fieldName, LocalDateTime.class, now);
                break;
            }
        }
    }
    
    /**
     * 填充创建人信息（支持多种字段名格式）
     */
    private void fillCreateUser(MetaObject metaObject, Long userId, String userName) {
        // 填充创建人ID
        String[] createUserIdFields = {"createUser", "createUserId", "createBy", "creatorId", "createdBy"};
        for (String fieldName : createUserIdFields) {
            if (metaObject.hasSetter(fieldName)) {
                this.strictInsertFill(metaObject, fieldName, Long.class, userId);
                break;
            }
        }
        
        // 填充创建人名称
        String[] createUserNameFields = {"createUserName", "createName", "creatorName", "createdByName"};
        for (String fieldName : createUserNameFields) {
            if (metaObject.hasSetter(fieldName)) {
                this.strictInsertFill(metaObject, fieldName, String.class, userName);
                break;
            }
        }
    }
    
    /**
     * 填充更新人信息（支持多种字段名格式）
     */
    private void fillUpdateUser(MetaObject metaObject, Long userId, String userName) {
        // 填充更新人ID
        String[] updateUserIdFields = {"updateUser", "updateUserId", "updateBy", "updaterId", "updatedBy"};
        for (String fieldName : updateUserIdFields) {
            if (metaObject.hasSetter(fieldName)) {
                this.strictInsertFill(metaObject, fieldName, Long.class, userId);
                this.strictUpdateFill(metaObject, fieldName, Long.class, userId);
                break;
            }
        }
        
        // 填充更新人名称
        String[] updateUserNameFields = {"updateUserName", "updateName", "updaterName", "updatedByName"};
        for (String fieldName : updateUserNameFields) {
            if (metaObject.hasSetter(fieldName)) {
                this.strictInsertFill(metaObject, fieldName, String.class, userName);
                this.strictUpdateFill(metaObject, fieldName, String.class, userName);
                break;
            }
        }
    }
    
    /**
     * 填充开始日期（针对fosters表的start_date字段）
     */
    private void fillStartDate(MetaObject metaObject, LocalDateTime now) {
        String[] startDateFields = {"startDate", "start_date"};
        for (String fieldName : startDateFields) {
            if (metaObject.hasSetter(fieldName)) {
                this.strictInsertFill(metaObject, fieldName, LocalDateTime.class, now);
                break;
            }
        }
    }
}