package com.example.petcommon.config;

import java.time.LocalDateTime;

import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    
    @Override
    public void insertFill(MetaObject metaObject) {

        LocalDateTime now = LocalDateTime.now();
        
        // 使用宽松模式填充创建时间，确保字段存在时才填充
        this.fillStrategy(metaObject, "createdAt", now);
        this.fillStrategy(metaObject, "created_at", now);
        this.fillStrategy(metaObject, "gmtCreate", now);
        // 添加对Users实体类createAt字段的支持
        this.fillStrategy(metaObject, "createAt", now);
        
        // 填充开始日期（针对fosters表的start_date字段）
        this.fillStrategy(metaObject, "startDate", now);
        this.fillStrategy(metaObject, "start_date", now);
        
        // 插入时也填充更新时间
        this.fillStrategy(metaObject, "updatedAt", now);
        this.fillStrategy(metaObject, "updateTime", now);
        this.fillStrategy(metaObject, "updatedTime", now);
        this.fillStrategy(metaObject, "gmtModified", now);
        this.fillStrategy(metaObject, "updated_at", now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        
        LocalDateTime now = LocalDateTime.now();
        
        // 填充更新时间字段
        fillUpdateTime(metaObject, now);

    }
    
    /**
     * 填充更新时间字段
     * @param metaObject 元对象
     * @param now 当前时间
     */
    private void fillUpdateTime(MetaObject metaObject, LocalDateTime now) {
        // 强制填充更新时间，确保每次更新都更新 updatedAt
        // 不区分实体类型，任何更新操作都应该更新 updatedAt
        // 使用strictUpdateFill确保即使字段已有值也会被更新
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, now);
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, now);
        this.strictUpdateFill(metaObject, "updatedTime", LocalDateTime.class, now);
        this.strictUpdateFill(metaObject, "gmtModified", LocalDateTime.class, now);
        this.strictUpdateFill(metaObject, "updated_at", LocalDateTime.class, now);
    }
}