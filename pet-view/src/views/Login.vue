<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>{{ t('login.title') }}</h2>
      </div>
      
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="username">{{ t('login.username') }}:</label>
          <input 
            type="text" 
            id="username" 
            v-model="form.username" 
            required 
            :placeholder="t('login.usernamePlaceholder')"
          />
        </div>
        
        <div class="form-group">
          <label for="password">{{ t('login.password') }}:</label>
          <input 
            type="password" 
            id="password" 
            v-model="form.password" 
            required 
            :placeholder="t('login.passwordPlaceholder')"
          />
        </div>
        
        <div class="form-group remember-me">
          <label class="checkbox-label">
            <input 
              type="checkbox" 
              v-model="form.rememberMe"
            />
            {{ t('login.rememberMe') }}
          </label>
        </div>
        
        <button type="submit" class="login-btn">
          {{ t('login.userLogin') }}
        </button>
        <div class="register-link">
          <router-link to="/register">
            {{ t('login.register') }}
          </router-link>
        </div>
      </form>
      
    </div>
  </div>
</template>

<script setup lang="ts">
import { useLogin } from '@/api/login'

// 使用登录逻辑
const { t, form, handleLogin, initForm } = useLogin()

// 初始化表单（自动填充记住的用户名）
initForm()
</script>


<style scoped >
@import '@/styles/login.css';
</style>
