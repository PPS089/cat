package com.example.petcommon.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * BeanUtils 工具类
 * 提供对象属性拷贝、转换等常用功能
 */
@Component
public class BeanUtilsHelper {

    @Autowired
    private BeanUtilsBean beanUtilsBean;

    @Autowired
    private ConvertUtilsBean convertUtilsBean;

    @Autowired
    private PropertyUtilsBean propertyUtilsBean;

    /**
     * 拷贝源对象的属性到目标对象
     * @param dest 目标对象
     * @param orig 源对象
     */
    public void copyProperties(Object dest, Object orig) {
        try {
            beanUtilsBean.copyProperties(dest, orig);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("属性拷贝失败", e);
        }
    }

    /**
     * 拷贝源对象的属性到目标对象（忽略null值）
     * 修正：解决Map<String, String>到Map<String, Object>的类型转换问题
     * @param dest 目标对象
     * @param orig 源对象
     */
    public void copyPropertiesIgnoreNull(Object dest, Object orig) {
        try {
            // 1. describe()返回的是Map<String, String>，直接声明为对应类型
            Map<String, String> stringProperties = beanUtilsBean.describe(orig);

            // 2. 转换为Map<String, Object>（避免后续类型转换错误）
            Map<String, Object> properties = new HashMap<>(stringProperties);

            properties.entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .forEach(entry -> {
                        try {
                            beanUtilsBean.setProperty(dest, entry.getKey(), entry.getValue());
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            // 忽略设置失败的属性
                        }
                    });
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("属性拷贝失败", e);
        }
    }

    /**
     * 将Map转换为对象
     * @param map Map数据
     * @param clazz 目标类
     * @param <T> 目标类型
     * @return 转换后的对象
     */
    public <T> T populate(Map<String, ? extends Object> map, Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            beanUtilsBean.populate(instance, map);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Map转换对象失败", e);
        }
    }

    /**
     * 将对象转换为Map
     * @param bean 对象
     * @return Map数据（值为String类型，因为describe()方法的特性）
     */
    public Map<String, String> describe(Object bean) {
        try {
            // 修正：返回类型与describe()方法保持一致（Map<String, String>）
            return beanUtilsBean.describe(bean);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("对象转换Map失败", e);
        }
    }

    /**
     * 获取对象的属性值
     * @param bean 对象
     * @param name 属性名
     * @return 属性值
     */
    public Object getProperty(Object bean, String name) {
        try {
            return beanUtilsBean.getProperty(bean, name);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("获取属性值失败", e);
        }
    }

    /**
     * 设置对象的属性值
     * @param bean 对象
     * @param name 属性名
     * @param value 属性值
     */
    public void setProperty(Object bean, String name, Object value) {
        try {
            beanUtilsBean.setProperty(bean, name, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("设置属性值失败", e);
        }
    }

    /**
     * 获取对象的属性类型
     * @param bean 对象
     * @param name 属性名
     * @return 属性类型
     */
    public Class<?> getPropertyType(Object bean, String name) {
        try {
            return propertyUtilsBean.getPropertyType(bean, name);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("获取属性类型失败", e);
        }
    }

    /**
     * 判断对象是否有指定属性
     * @param bean 对象
     * @param name 属性名
     * @return 是否有该属性
     */
    public boolean hasProperty(Object bean, String name) {
        try {
            // 检查属性是否可读（根据commons-beanutils 1.9.4版本，此方法可能不抛出检查异常，或仅抛出部分异常）
            return propertyUtilsBean.isReadable(bean, name);
        } catch (Exception e) {
            // 改为捕获通用Exception（容错处理，避免因异常声明差异导致编译错误）
            return false;
        }
    }
}