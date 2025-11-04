<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-left">
        <div class="image-container">
          <img src="@/assets/img/dog.jpg" alt="宠物" class="login-image" />
          <div class="image-overlay">
            <h1 class="welcome-title">{{ t('login.welcomeTitle') || '欢迎回来' }}</h1>
            <p class="welcome-subtitle">{{ t('login.welcomeSubtitle') || '登录您的账户，继续您的宠物之旅' }}</p>
          </div>
        </div>
      </div>
      
      <div class="login-right">
        <div class="form-header">
          <h2 class="form-title">{{ t('login.title') }}</h2>
          <p class="form-subtitle">{{ t('login.subtitle') || '请输入您的账户信息' }}</p>
        </div>
        
        <el-form :model="form" ref="loginForm" label-width="0" class="login-form">
          <el-form-item>
            <el-input 
              v-model="form.username" 
              :placeholder="t('login.usernamePlaceholder')"
              size="large"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item>
            <el-input 
              v-model="form.password" 
              type="password" 
              :placeholder="t('login.passwordPlaceholder')"
              size="large"
              show-password
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item class="remember-me-item">
            <el-checkbox v-model="form.rememberMe" class="remember-me-checkbox">
              {{ t('login.rememberMe') }}
            </el-checkbox>
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              @click="handleLogin" 
              size="large" 
              class="submit-btn"
              :loading="loading"
            >
              {{ t('login.userLogin') }}
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="form-footer">
          <p class="register-text">
            {{ t('login.noAccount') || '还没有账户？' }}
            <el-link type="primary" @click="goToRegister" :underline="false">
              {{ t('login.registerLink') }}
            </el-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { useLogin } from '@/api/login'

const router = useRouter()
const loading = ref(false)

// 使用登录逻辑
const { t, form, handleLogin: originalHandleLogin } = useLogin()


// 处理登录
const handleLogin = async () => {
  loading.value = true
  try {
    await originalHandleLogin()
  } finally {
    loading.value = false
  }
}

// 跳转到注册页面
const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
@import '@/styles/login.css';
</style>
