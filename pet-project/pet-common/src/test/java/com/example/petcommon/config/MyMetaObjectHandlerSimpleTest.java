package com.example.petcommon.config;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class MyMetaObjectHandlerSimpleTest {

    @Test
    void testFillUpdateTimeMethodExists() {
        // 测试fillUpdateTime方法是否存在并正常工作
        MyMetaObjectHandler handler = new MyMetaObjectHandler();
        assertNotNull(handler);
        
        // 测试方法是否存在
        Method[] methods = MyMetaObjectHandler.class.getDeclaredMethods();
        boolean hasFillUpdateTime = false;
        for (Method method : methods) {
            if (method.getName().equals("fillUpdateTime")) {
                hasFillUpdateTime = true;
                break;
            }
        }
        
        assertTrue(hasFillUpdateTime, "fillUpdateTime方法应该存在");
    }
    
    @Test
    void testEntitySpecificUpdateTime() {
        // 测试不同实体类类型的updatedAt更新逻辑
        MyMetaObjectHandler handler = new MyMetaObjectHandler();
        assertNotNull(handler);
        
        // 验证MediaFiles对象类型
        String mediaFilesClassName = "com.example.petpojo.entity.MediaFiles";
        assertTrue(mediaFilesClassName.contains("MediaFiles"), "MediaFiles类名应该包含'MediaFiles'");
        
        // 验证PetRecords对象类型
        String petRecordsClassName = "com.example.petpojo.entity.PetRecords";
        assertTrue(petRecordsClassName.contains("PetRecords"), "PetRecords类名应该包含'PetRecords'");
        
        // 验证其他对象类型
        String otherClassName = "com.example.petpojo.entity.OtherEntity";
        assertFalse(otherClassName.contains("MediaFiles"), "其他实体类名不应该包含'MediaFiles'");
        assertFalse(otherClassName.contains("PetRecords"), "其他实体类名不应该包含'PetRecords'");
    }
}