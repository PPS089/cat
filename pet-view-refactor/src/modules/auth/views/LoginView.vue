<template>
  <AuthFormShell
    :title="t('login.title')"
    :subtitle="t('common.appName')"
    :description="t('login.subtitle')"
    :hero-title="t('login.welcomeTitle') || t('common.appName')"
    :hero-description="t('login.welcomeSubtitle') || t('login.subtitle')"
    :hero-image="heroImage"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="0" class="auth-form login-form">
      <el-form-item prop="username">
        <el-input
          v-model="form.username"
          :placeholder="t('login.usernamePlaceholder')"
          size="large"
          autocomplete="username"
        >
          <template #prefix>
            <el-icon><User /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="form.password"
          type="password"
          :placeholder="t('login.passwordPlaceholder')"
          autocomplete="current-password"
          show-password
          size="large"
        >
          <template #prefix>
            <el-icon><Lock /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item class="auth-form__meta-row">
        <el-checkbox v-model="form.rememberMe" class="remember-me-checkbox">
          {{ t('login.rememberMe') }}
        </el-checkbox>
        <el-link type="primary" :underline="false" @click="goToForgotPassword">
          {{ t('login.forgotPassword') }}
        </el-link>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="onSubmit">
          {{ t('login.userLogin') }}
        </el-button>
      </el-form-item>
    </el-form>
    <template #footer>
      <p class="auth-form__footer-text">
        {{ t('login.noAccount') }}
        <el-link type="primary" :underline="false" @click="goToRegister">
          {{ t('login.registerLink') }}
        </el-link>
      </p>
    </template>
  </AuthFormShell>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElForm, ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/app/store/modules/user'
import AuthFormShell from '@/modules/auth/components/AuthFormShell.vue'
import { User, Lock } from '@element-plus/icons-vue'
import { consumeAuthNotice } from '@/shared/utils/request'
import heroImage from '@/assets/img/dog.jpg'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const formRef = ref<InstanceType<typeof ElForm>>()
const form = reactive({
  username: '',
  password: '',
  rememberMe: false,
})

const rules = {
  username: [
    { required: true, message: t('login.usernamePlaceholder'), trigger: 'blur' },
    { min: 1, max: 10, message: t('user.usernameLength'), trigger: 'blur' },
  ],
  password: [
    { required: true, message: t('login.passwordPlaceholder'), trigger: 'blur' },
    { min: 6, max: 12, message: t('login.passwordLength'), trigger: 'blur' },
  ],
}

onMounted(() => {
  const notice = consumeAuthNotice()
  if (notice === 'expired' || notice === 'refresh_failed') {
    ElMessage.error('登录已过期，请重新登录')
  } else if (notice === 'unauthorized') {
    ElMessage.error('请先登录')
  }

  const rememberedUsername = localStorage.getItem('rememberedUsername')
  const rememberedPassword = localStorage.getItem('rememberedPassword')
  if (rememberedUsername && rememberedPassword) {
    form.username = rememberedUsername
    form.password = rememberedPassword
    form.rememberMe = true
  }
})

const onSubmit = async () => {
  await formRef.value?.validate(async valid => {
    if (!valid) return
    loading.value = true
    try {
      const loginResp = await userStore.login({ username: form.username.trim(), password: form.password.trim() })
      await userStore.fetchProfile()
      if (form.rememberMe) {
        localStorage.setItem('rememberedUsername', form.username)
        localStorage.setItem('rememberedPassword', form.password)
      } else {
        localStorage.removeItem('rememberedUsername')
        localStorage.removeItem('rememberedPassword')
      }
      const isAdmin = (loginResp.role || '').toUpperCase() === 'ADMIN'
      const redirect = (route.query.redirect as string) || (isAdmin ? '/admin' : '/user')
      router.push(redirect)
    } catch (error: any) {
      console.error('[Login] login failed', error)
    } finally {
      loading.value = false
    }
  })
}

const goToRegister = () => router.push('/register')
const goToForgotPassword = () => router.push('/forgot-password')
</script>
