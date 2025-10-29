package com.example.petcommon.config;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * BeanUtils 配置类
 * 提供 BeanUtilsBean、ConvertUtilsBean、PropertyUtilsBean 的 Bean 定义
 */
@Configuration
public class BeanUtilsConfig {

    @Bean
    public BeanUtilsBean beanUtilsBean() {
        return new BeanUtilsBean();
    }

    @Bean
    public ConvertUtilsBean convertUtilsBean() {
        return new ConvertUtilsBean();
    }

    @Bean
    public PropertyUtilsBean propertyUtilsBean() {
        return new PropertyUtilsBean();
    }
}