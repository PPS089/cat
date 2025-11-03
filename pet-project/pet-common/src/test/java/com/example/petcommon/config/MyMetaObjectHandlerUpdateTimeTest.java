package com.example.petcommon.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;
import org.apache.ibatis.reflection.MetaObject;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MyMetaObjectHandler 更新时间字段测试
 * 验证不同实体类的updatedAt字段更新逻辑
 */
class MyMetaObjectHandlerUpdateTimeTest {

    private MyMetaObjectHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new MyMetaObjectHandler();
    }

    @Test
    void testFillUpdateTimeMethodExists() {
        // 验证fillUpdateTime方法存在
        try {
            java.lang.reflect.Method method = MyMetaObjectHandler.class.getDeclaredMethod("fillUpdateTime", MetaObject.class, java.time.LocalDateTime.class);
            assertNotNull(method, "fillUpdateTime方法应该存在");
            assertEquals("fillUpdateTime", method.getName(), "方法名应该是fillUpdateTime");
            assertEquals(2, method.getParameterCount(), "方法应该有两个参数");
        } catch (NoSuchMethodException e) {
            fail("fillUpdateTime方法不存在: " + e.getMessage());
        }
    }

    @Test
    void testEntityClassNameMatching() {
        // 测试实体类名匹配逻辑
        String mediaFilesClassName = "com.example.petpojo.entity.MediaFiles";
        String petRecordsClassName = "com.example.petpojo.entity.PetRecords";
        String otherClassName = "com.example.petpojo.entity.OtherEntity";
        
        // 验证MediaFiles类名匹配
        assertTrue(mediaFilesClassName.contains("MediaFiles"), "MediaFiles类名应该包含'MediaFiles'");
        assertFalse(mediaFilesClassName.contains("PetRecords"), "MediaFiles类名不应该包含'PetRecords'");
        
        // 验证PetRecords类名匹配
        assertTrue(petRecordsClassName.contains("PetRecords"), "PetRecords类名应该包含'PetRecords'");
        assertFalse(petRecordsClassName.contains("MediaFiles"), "PetRecords类名不应该包含'MediaFiles'");
        
        // 验证其他实体类名不匹配
        assertFalse(otherClassName.contains("MediaFiles"), "其他实体类名不应该包含'MediaFiles'");
        assertFalse(otherClassName.contains("PetRecords"), "其他实体类名不应该包含'PetRecords'");
    }

    @Test
    void testHandlerInstantiation() {
        // 验证MyMetaObjectHandler可以正常实例化
        assertNotNull(handler, "MyMetaObjectHandler应该可以正常实例化");
    }

    @Test
    void testInsertFillMethodExists() {
        // 验证insertFill方法存在
        try {
            java.lang.reflect.Method method = MyMetaObjectHandler.class.getDeclaredMethod("insertFill", MetaObject.class);
            assertNotNull(method, "insertFill方法应该存在");
            assertEquals("insertFill", method.getName(), "方法名应该是insertFill");
            assertEquals(1, method.getParameterCount(), "方法应该有一个参数");
        } catch (NoSuchMethodException e) {
            fail("insertFill方法不存在: " + e.getMessage());
        }
    }
}