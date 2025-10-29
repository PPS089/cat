<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-left">
        <div class="image-container">
          <img src="@/assets/img/dog.jpg" alt="宠物" class="register-image" />
          <div class="image-overlay">
            <h1 class="welcome-title">{{ t('register.welcomeTitle') }}</h1>
            <p class="welcome-subtitle">{{ t('register.welcomeSubtitle') }}</p>
          </div>
        </div>
      </div>
      
      <div class="register-right">
        <div class="form-header">
          <h2 class="form-title">{{ t('register.createAccount') }}</h2>
          <p class="form-subtitle">{{ t('register.fillInfo') }}</p>
        </div>
        
        <el-form :model="form" :rules="rules" ref="registerForm" label-width="0" class="register-form">
          <el-form-item prop="username">
            <el-input 
              v-model="form.username" 
              :placeholder="t('register.usernamePlaceholder')"
              size="large"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="phone">
            <el-input 
              v-model="form.phone" 
              :placeholder="t('register.phonePlaceholder')"
              size="large"
            >
              <template #prefix>
                <el-icon><Phone /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="email">
            <el-input 
              v-model="form.email" 
              :placeholder="t('register.emailPlaceholder')"
              size="large"
            >
              <template #prefix>
                <el-icon><Message /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              :placeholder="t('register.passwordPlaceholder')"
              size="large"
              show-password
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="confirmPassword">
            <el-input 
              v-model="form.confirmPassword" 
              type="password" 
              :placeholder="t('register.confirmPasswordPlaceholder')"
              size="large"
              show-password
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              @click="onSubmit" 
              size="large" 
              class="submit-btn"
              :loading="loading"
            >
              {{ t('register.submit') }}
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="form-footer">
          <p class="login-text">
            {{ t('register.hasAccount') }}
            <el-link type="primary" @click="login" :underline="false">
              {{ t('register.loginNow') }}
            </el-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRegister } from '@/api/register'


// 使用注册逻辑
const {
  // 数据
  form,
  loading,
  rules,
  
  // 图标
  User,
  Phone,
  Lock,
  Message,
  
  // 方法
  login,
  onSubmit,
  
  // 翻译函数
  t
} = useRegister()
</script>

<style scoped>
@import '@/styles/register.css';
</style>