<template>
  <AuthFormShell
    :title="currentStep === 'send-code' ? (t('forgotPassword.step1') || t('forgotPassword.title')) : (t('forgotPassword.step2') || t('forgotPassword.title'))"
    :subtitle="t('common.appName')"
    :description="currentStep === 'send-code' ? (t('forgotPassword.step1Desc') || t('forgotPassword.subtitle')) : (t('forgotPassword.step2Desc') || t('forgotPassword.subtitle'))"
    :hero-title="t('forgotPassword.title')"
    :hero-description="t('forgotPassword.subtitle')"
    :hero-image="heroImage"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="0" class="auth-form forgot-form">
      <el-form-item v-show="currentStep === 'send-code'" prop="email">
        <el-input
          v-model="form.email"
          :placeholder="t('forgotPassword.emailPlaceholder') || '请输入注册邮箱'"
          size="large"
          autocomplete="email"
        >
          <template #prefix>
            <el-icon><Message /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <template v-if="currentStep === 'send-code'">
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="sendingCode"
            :disabled="countdown > 0"
            @click="handleSendCode"
          >
            {{ countdown > 0 ? countdownLabel : (t('forgotPassword.sendCodeBtn') || t('forgotPassword.sendCode')) }}
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button class="auth-ghost-button" size="large" @click="goToLogin">
            {{ t('forgotPassword.backToLogin') || t('login.userLogin') }}
          </el-button>
        </el-form-item>
      </template>
      <template v-else>
        <div class="email-hint">
          <span>{{ form.email }}</span>
          <el-link type="primary" :underline="false" @click="backToEmail">
            {{ t('forgotPassword.changeEmail') || '修改邮箱' }}
          </el-link>
        </div>
        <el-form-item prop="code">
          <el-input
            v-model="form.code"
            :placeholder="t('forgotPassword.codePlaceholder') || '请输入验证码'"
            size="large"
            autocomplete="one-time-code"
          >
            <template #prefix>
              <el-icon><Key /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="newPassword">
          <el-input
            v-model="form.newPassword"
            :placeholder="t('forgotPassword.newPasswordPlaceholder') || '请输入新密码（至少6位）'"
            type="password"
            show-password
            size="large"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            :placeholder="t('forgotPassword.confirmPasswordPlaceholder') || '请确认新密码'"
            type="password"
            show-password
            size="large"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleResetPassword"
          >
            {{ t('forgotPassword.resetPasswordBtn') || t('forgotPassword.submit') }}
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button class="auth-ghost-button" size="large" :disabled="countdown > 0" @click="handleResend">
            {{ countdown > 0 ? countdownLabel : (t('forgotPassword.resendCode') || t('forgotPassword.sendCode')) }}
          </el-button>
        </el-form-item>
      </template>
    </el-form>
    <template #footer>
      <p class="auth-form__footer-text">
        {{ t('forgotPassword.backToLogin') || t('login.userLogin') }}
        <el-link type="primary" :underline="false" @click="goToLogin">
          {{ t('login.userLogin') }}
        </el-link>
      </p>
    </template>
  </AuthFormShell>
</template>

<script setup lang="ts">
import { computed, reactive, ref, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElForm, ElMessage } from 'element-plus'
import type { FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { authService } from '@/modules/auth/services/auth'
import AuthFormShell from '@/modules/auth/components/AuthFormShell.vue'
import { Message, Lock, Key } from '@element-plus/icons-vue'
import heroImage from '@/assets/img/2.jpg'

const { t } = useI18n()
const router = useRouter()
const formRef = ref<InstanceType<typeof ElForm>>()
const loading = ref(false)
const sendingCode = ref(false)
const COUNTDOWN_SECONDS = 180
const countdown = ref(0)
const currentStep = ref<'send-code' | 'reset'>('send-code')
let timer: ReturnType<typeof setInterval> | null = null

const form = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: '',
})

const PASSWORD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)\S{6,12}$/

const rules = reactive<FormRules>({
  email: [
    { required: true, message: t('register.emailRequired'), trigger: 'blur' },
    { type: 'email', message: t('register.emailInvalid'), trigger: 'blur' },
  ],
  code: [
    { required: true, message: t('forgotPassword.codeRequired'), trigger: 'blur' },
    { min: 4, max: 8, message: t('forgotPassword.codeLength'), trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: t('register.passwordRequired'), trigger: 'blur' },
    { pattern: PASSWORD_REGEX, message: t('register.passwordFormatError'), trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: t('register.confirmPasswordRequired'), trigger: 'blur' },
    {
      validator: (_: unknown, value: string, callback: (err?: Error) => void) => {
        if (value !== form.newPassword) {
          callback(new Error(t('register.passwordMismatch')))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
})

const countdownLabel = computed(() => {
  const seconds = Math.max(0, countdown.value)
  const minutes = Math.floor(seconds / 60)
  const remainder = seconds % 60
  return `${String(minutes).padStart(2, '0')}:${String(remainder).padStart(2, '0')}`
})

const clearTimer = () => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

const resetVerificationFields = () => {
  form.code = ''
}

const handleCountdownExpired = () => {
  clearTimer()
  countdown.value = 0
  resetVerificationFields()
  ElMessage.info(t('forgotPassword.codeExpired') || '验证码已过期，请重新发送')
}

const startCountdown = () => {
  countdown.value = COUNTDOWN_SECONDS
  clearTimer()
  timer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) {
      handleCountdownExpired()
    }
  }, 1000)
}

const handleSendCode = async () => {
  if (countdown.value > 0) return
  try {
    await formRef.value?.validateField('email')
  } catch {
    return
  }
  sendingCode.value = true
  try {
    await authService.sendResetCode(form.email.trim())
    ElMessage.success(t('forgotPassword.codeSent'))
    startCountdown()
    currentStep.value = 'reset'
  } catch (error: any) {
    console.error('[ForgotPassword] send code failed', error)
    if (error?.response?.data?.code === 404001) {
      countdown.value = 0
      clearTimer()
      currentStep.value = 'send-code'
    }
  } finally {
    sendingCode.value = false
  }
}

const handleResetPassword = async () => {
  await formRef.value?.validate(async valid => {
    if (!valid) return
    loading.value = true
    try {
      await authService.resetPassword({
        email: form.email.trim(),
        code: form.code.trim(),
        newPassword: form.newPassword.trim(),
      })
      ElMessage.success(t('forgotPassword.success'))
      router.push('/login')
    } catch (error: any) {
      console.error('[ForgotPassword] reset password failed', error)
    } finally {
      loading.value = false
    }
  })
}

const handleResend = async () => {
  if (countdown.value > 0) return
  await handleSendCode()
}

const backToEmail = () => {
  currentStep.value = 'send-code'
  clearTimer()
  countdown.value = 0
  resetVerificationFields()
  form.newPassword = ''
  form.confirmPassword = ''
}

const goToLogin = () => {
  backToEmail()
  router.push('/login')
}

onBeforeUnmount(() => {
  clearTimer()
})
</script>

<style scoped>
.email-hint {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9rem;
  color: #475569;
  margin-bottom: 0.5rem;
}
</style>
